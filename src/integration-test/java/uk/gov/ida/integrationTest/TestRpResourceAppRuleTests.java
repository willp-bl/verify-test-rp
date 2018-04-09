package uk.gov.ida.integrationTest;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.util.Duration;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import uk.gov.ida.integrationTest.support.JerseyGuiceIntegrationTestAdapter;
import uk.gov.ida.integrationTest.support.TestRpAppRule;
import uk.gov.ida.jerseyclient.JerseyClientConfigurationBuilder;
import uk.gov.ida.rp.testrp.MsaStubRule;
import uk.gov.ida.rp.testrp.Urls;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class TestRpResourceAppRuleTests extends JerseyGuiceIntegrationTestAdapter {
    private static Client client;

    private static WireMockRule msaStubRule = MsaStubRule.create("metadata.xml");

    private static TestRpAppRule testRp = TestRpAppRule.newTestRpAppRule(
            ConfigOverride.config("clientTrustStoreConfiguration.path", ResourceHelpers.resourceFilePath("ida_truststore.ts")),
            ConfigOverride.config("msaMetadataUri", "http://localhost:5555/metadata"),
            ConfigOverride.config("allowInsecureMetadataLocation", "true"));

    @ClassRule
    public static TestRule chain = RuleChain.outerRule(msaStubRule).around(testRp);

    @BeforeClass
    public static void beforeClass() {
        JerseyClientConfiguration jerseyClientConfiguration = JerseyClientConfigurationBuilder.aJerseyClientConfiguration().withTimeout(Duration.seconds(20)).build();
        client = new JerseyClientBuilder(testRp.getEnvironment()).using(jerseyClientConfiguration).build(TestRpResourceAppRuleTests.class.getSimpleName());
    }

    @Test
    public void shouldReturnSamlRedirectView() throws Exception {
        URI uri = testRp.uri(Urls.TestRpUrls.TEST_RP_ROOT + Urls.SUCCESSFUL_REGISTER_PATH);
        Response response = client
                .target(uri)
                .queryParam(Urls.Params.RP_NAME_PARAM, "test-rp")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        String html = response.readEntity(String.class);
        assertThat(html).contains("Saml Processing...");
        assertThat(html).contains("form");
    }
}
