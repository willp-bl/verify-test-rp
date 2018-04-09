package uk.gov.ida.rp.testrp.tokenservice;

import uk.gov.ida.rp.testrp.domain.AccessToken;
import uk.gov.ida.rp.testrp.exceptions.InvalidAccessTokenException;

import javax.inject.Inject;
import java.util.Optional;

public class TokenServiceAccessValidator implements AccessTokenValidator {

    private TokenServiceClient tokenServiceClient;

    @Inject
    public TokenServiceAccessValidator(TokenServiceClient tokenServiceClient) {
        this.tokenServiceClient = tokenServiceClient;
    }

    @Override
    public void validate(Optional<AccessToken> token) {
        if (!token.isPresent()) {
            throw new InvalidAccessTokenException("Token must be provided.");
        }

        TokenValidationResponse tokenValidationResponse = tokenServiceClient.validateToken(token.get());

        if(!tokenValidationResponse.isValid()){
            throw new InvalidAccessTokenException("Token is invalid.");
        }
    }
}
