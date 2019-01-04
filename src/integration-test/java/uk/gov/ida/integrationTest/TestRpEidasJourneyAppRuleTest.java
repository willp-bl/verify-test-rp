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
import uk.gov.ida.jerseyclient.JerseyClientConfigurationBuilder;
import uk.gov.ida.rp.testrp.Urls;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.junit.Assert.assertTrue;

public class TestRpEidasJourneyAppRuleTest extends IntegrationTestHelper {
    private static Client client;

    @ClassRule
    public static TestRpAppRule testRp = TestRpAppRule.newTestRpAppRule();

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
