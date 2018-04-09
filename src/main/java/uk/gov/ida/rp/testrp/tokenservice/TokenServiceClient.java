package uk.gov.ida.rp.testrp.tokenservice;

import org.apache.http.HttpHeaders;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.domain.AccessToken;
import uk.gov.ida.rp.testrp.exceptions.InvalidAccessTokenException;
import uk.gov.ida.rp.testrp.exceptions.TokenServiceUnavailableException;

import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import static java.text.MessageFormat.format;
import static org.glassfish.jersey.client.authentication.HttpAuthenticationFeature.basic;

public class TokenServiceClient {

    public static final String VALIDATE_PATH = "{0}/tokens/{1}/validate";
    private final TestRpConfiguration testRpConfiguration;
    private final Client client;

    @Inject
    public TokenServiceClient(TestRpConfiguration testRpConfiguration, Client client) {
        this.testRpConfiguration = testRpConfiguration;
        this.client = client;
    }

    public TokenValidationResponse validateToken(AccessToken token) {
        String url = format(VALIDATE_PATH, testRpConfiguration.getTokenServiceUrl(), token.toString());
        HttpAuthenticationFeature authenticationFeature = basic(testRpConfiguration.getTokenServiceUser(), testRpConfiguration.getTokenServicePassword());
        client.register(authenticationFeature);

        try {
            return client
                .target(url)
                .request()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                    .post(null, TokenValidationResponse.class);
        } catch (ProcessingException e) {
            throw new TokenServiceUnavailableException("Token service did not respond.");
        } catch (WebApplicationException e) {
            throw new InvalidAccessTokenException("Token service failed validation.");
        }


    }
}
