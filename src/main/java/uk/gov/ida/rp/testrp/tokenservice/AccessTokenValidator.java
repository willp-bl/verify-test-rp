package uk.gov.ida.rp.testrp.tokenservice;

import uk.gov.ida.rp.testrp.domain.AccessToken;

import java.util.Optional;

public interface AccessTokenValidator {

    void validate(Optional<AccessToken> token);
}
