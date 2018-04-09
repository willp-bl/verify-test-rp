package uk.gov.ida.integrationTest;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import uk.gov.ida.integrationTest.support.JerseyGuiceIntegrationTestAdapter;
import uk.gov.ida.integrationTest.support.TestRpAppRule;
import uk.gov.ida.rp.testrp.MsaStubRule;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminPortAppRuleTests extends JerseyGuiceIntegrationTestAdapter {
    private static Client client = JerseyClientBuilder.createClient().property(ClientProperties.FOLLOW_REDIRECTS, false);

    private static WireMockRule msaStubRule = MsaStubRule.create("metadata.xml");
    private static TestRpAppRule testRp = TestRpAppRule.newTestRpAppRule(
            ConfigOverride.config("clientTrustStoreConfiguration.path", ResourceHelpers.resourceFilePath("ida_truststore.ts")),
            ConfigOverride.config("msaMetadataUri", "http://localhost:5555/metadata"),
            ConfigOverride.config("allowInsecureMetadataLocation", "true"));

    @ClassRule
    public static TestRule chain = RuleChain.outerRule(msaStubRule).around(testRp);

    @Test
    public void ensureHealthCheckRegistered() {
        final Response response = client.target("http://localhost:" + testRp.getAdminPort() + "/healthcheck").request().get();
        assertThat(response.getStatus()).isEqualTo(200);
        final String entity = response.readEntity(String.class);
        assertThat(entity).contains("metadata");
    }

    @Test
    public void ensureMetadataRefreshTaskRegistered() {
        final Response response = client.target("http://localhost:" + testRp.getAdminPort() + "/tasks/metadata-refresh").request().post(Entity.text(""));
        assertThat(response.getStatus()).isEqualTo(200);
    }

}
