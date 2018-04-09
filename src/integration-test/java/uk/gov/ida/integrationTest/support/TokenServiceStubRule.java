package uk.gov.ida.integrationTest.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import httpstub.ExpectedRequest;
import httpstub.HttpStubRule;
import httpstub.RegisteredResponse;
import httpstub.builders.ExpectedRequestBuilder;
import httpstub.builders.RegisteredResponseBuilder;
import org.apache.http.entity.ContentType;
import uk.gov.ida.rp.testrp.tokenservice.TokenValidationResponse;

import static java.text.MessageFormat.format;

public class TokenServiceStubRule extends HttpStubRule {
    public void theTokenServiceIsUnavailable(String token) {
        register(format("http://localhost:53100/tokens/{0}/validate", token), 503);
    }

    public void stubValidTokenResponse(String token) throws JsonProcessingException {
        RegisteredResponse registeredResponse = RegisteredResponseBuilder.aRegisteredResponse()
                .withBody(new TokenValidationResponse(true))
                .withContentType(ContentType.APPLICATION_JSON.toString())
                .withStatus(200)
                .build();
        String path = format("/tokens/{0}/validate", token);
        ExpectedRequest expectedRequest = ExpectedRequestBuilder.expectRequest()
                .withPath(path)
                .withMethod("POST")
                .withBody("")
                .withHeaders(null)
                .build();
        register(expectedRequest, registeredResponse);
    }

    public void stubInvalidTokenResponse(String token) throws JsonProcessingException {
        RegisteredResponse registeredResponse = RegisteredResponseBuilder.aRegisteredResponse()
                .withBody(new TokenValidationResponse(false))
                .withContentType(ContentType.APPLICATION_JSON.toString())
                .withStatus(200)
                .build();
        String path = format("/tokens/{0}/validate", token);
        ExpectedRequest expectedRequest = ExpectedRequestBuilder.expectRequest()
                .withPath(path)
                .withMethod("POST")
                .withBody("")
                .withHeaders(null)
                .build();
        register(expectedRequest, registeredResponse);
    }
}
