package uk.gov.ida.integrationTest;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.util.Duration;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.integrationTest.support.IntegrationTestHelper;
import uk.gov.ida.integrationTest.support.TestRpAppRule;
import uk.gov.ida.integrationTest.support.TokenServiceStubRule;
import uk.gov.ida.jerseyclient.JerseyClientConfigurationBuilder;
import uk.gov.ida.rp.testrp.Urls;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class TestRpTokenServiceUnavailableAppRuleTests extends IntegrationTestHelper {

    private static Client client;

    private static final String AUTHORIZED_TOKEN_VALUE = "foovalue";

    @ClassRule
    public static TestRpAppRule testRp = TestRpAppRule.newTestRpAppRule(
            ConfigOverride.config("clientTrustStoreConfiguration.path", ResourceHelpers.resourceFilePath("ida_truststore.ts")),
            ConfigOverride.config("privateBetaUserAccessRestrictionEnabled", "true")
            );

    @ClassRule
    public static TokenServiceStubRule tokenServiceStubRule = new TokenServiceStubRule();

    @BeforeClass
    public static void beforeClass() {
        JerseyClientConfiguration jerseyClientConfiguration = JerseyClientConfigurationBuilder.aJerseyClientConfiguration().withTimeout(Duration.seconds(20)).build();
        client = new JerseyClientBuilder(testRp.getEnvironment()).using(jerseyClientConfiguration).build(TestRpTokenServiceUnavailableAppRuleTests.class.getSimpleName());
    }

    @Test
    public void getLandingPage_whenTokenServiceIsUnavailable_shouldReturnTokenServiceUnavailablePage() {
        tokenServiceStubRule.theTokenServiceIsUnavailable(AUTHORIZED_TOKEN_VALUE);

        Response response = client.target(testRp.uri(Urls.TestRpUrls.TEST_RP_ROOT))
                .queryParam(Urls.Params.ACCESS_TOKEN_PARAM, AUTHORIZED_TOKEN_VALUE)
                .request(MediaType.TEXT_HTML)
                .get(Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(response.readEntity(String.class)).contains("Identity Assurance Test Service - Please Retry - GOV.UK");
    }
}
