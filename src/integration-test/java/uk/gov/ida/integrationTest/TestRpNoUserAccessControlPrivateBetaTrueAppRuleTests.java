package uk.gov.ida.integrationTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.util.Duration;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.integrationTest.support.JerseyGuiceIntegrationTestAdapter;
import uk.gov.ida.integrationTest.support.TestRpAppRule;
import uk.gov.ida.integrationTest.support.TokenServiceStubRule;
import uk.gov.ida.jerseyclient.JerseyClientConfigurationBuilder;
import uk.gov.ida.rp.testrp.Urls;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class TestRpNoUserAccessControlPrivateBetaTrueAppRuleTests extends JerseyGuiceIntegrationTestAdapter {

    private static Client client;

    private static final String AUTHORIZED_TOKEN_VALUE = "foovalue";

    @ClassRule
    public static TokenServiceStubRule tokenServiceStubRule = new TokenServiceStubRule();

    @ClassRule
    public static TestRpAppRule testRp = TestRpAppRule.newTestRpAppRule(
            ConfigOverride.config("clientTrustStoreConfiguration.path", ResourceHelpers.resourceFilePath("ida_truststore.ts")),
            ConfigOverride.config("privateBetaUserAccessRestrictionEnabled", "true"),
            ConfigOverride.config("tokenServiceUrl", tokenServiceStubRule.baseUri().build().toASCIIString())
    );

    @BeforeClass
    public static void beforeClass() {
        JerseyClientConfiguration jerseyClientConfiguration = JerseyClientConfigurationBuilder.aJerseyClientConfiguration().withTimeout(Duration.seconds(20)).build();
        client = new JerseyClientBuilder(testRp.getEnvironment()).using(jerseyClientConfiguration).build(TestRpNoUserAccessControlPrivateBetaTrueAppRuleTests.class.getSimpleName());
    }

    @Before
    public void resetStubRules() {
        tokenServiceStubRule.reset();
    }

    @Test
    public void getLandingPage_withValidToken_shouldReturnTestRpLandingPageView() throws JsonProcessingException {
        tokenServiceStubRule.stubValidTokenResponse(AUTHORIZED_TOKEN_VALUE);

        URI uri = testRp.uri(Urls.TestRpUrls.TEST_RP_ROOT);
        Response response = client.target(uri)
                .queryParam(Urls.Params.ACCESS_TOKEN_PARAM, AUTHORIZED_TOKEN_VALUE)
                .request(MediaType.TEXT_HTML)
                .get(Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.readEntity(String.class)).contains("Register for an identity profile");
    }

    @Test
    public void getLandingPage_withInvalidToken_shouldReturnTestRpLandingPage() throws JsonProcessingException {
        String invalidToken = "some-invalid-value";
        tokenServiceStubRule.stubInvalidTokenResponse(invalidToken);

        Response response = client.target(testRp.uri(Urls.TestRpUrls.TEST_RP_ROOT))
                .queryParam(Urls.Params.ACCESS_TOKEN_PARAM, invalidToken)
                .request(MediaType.TEXT_HTML)
                .get(Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(response.readEntity(String.class)).contains("The Identity Assurance Test Service is for testing purposes only, and is only open to invited participants.");
    }

    @Test
    public void getLandingPage_withMissingToken_shouldReturnTestRpLandingPage() throws JsonProcessingException {

        tokenServiceStubRule.stubInvalidTokenResponse("");
        Response response = client.target(testRp.uri(Urls.TestRpUrls.TEST_RP_ROOT))
                .request()
                .get(Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(response.readEntity(String.class)).contains("The Identity Assurance Test Service is for testing purposes only, and is only open to invited participants.");
    }
}
