package uk.gov.ida.rp.testrp.tokenservice;

import uk.gov.ida.rp.testrp.domain.AccessToken;

import java.util.Optional;

public class NoOpAccessTokenValidator implements AccessTokenValidator {

    @Override
    public void validate(Optional<AccessToken> token) {
        // this comment intentionally left blank
    }
}
