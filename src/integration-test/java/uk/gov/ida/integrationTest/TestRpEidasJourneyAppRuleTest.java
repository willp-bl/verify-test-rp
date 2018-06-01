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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.junit.Assert.assertTrue;

public class TestRpEidasJourneyAppRuleTest extends JerseyGuiceIntegrationTestAdapter {
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
    public void getTestRpSamlRedirectView_shouldHaveEidasJourneyHint() {
        URI uri = testRp.uriBuilder(Urls.TestRpUrls.SUCCESSFUL_REGISTER_RESOURCE)
            .queryParam("eidas", "true")
            .build();

        Response response = client.target(uri)
            .request(MediaType.TEXT_HTML)
            .get(Response.class);

        String html = response.readEntity(String.class);
        assertTrue(html.contains("name=\"eidas_journey\""));
    }

    @Test
    public void getHeadlessRpSamlRedirectView_shouldHaveEidasJourneyHint() {
        URI uri = testRp.uriBuilder(Urls.HeadlessUrls.SUCCESS_PATH)
            .queryParam("eidas", "true")
            .build();

        Response response = client.target(uri)
            .request(MediaType.TEXT_HTML)
            .get(Response.class);

        String html = response.readEntity(String.class);
        assertTrue(html.contains("name=\"eidas_journey\""));
    }

}
