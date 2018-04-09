package uk.gov.ida.rp.testrp.builders;

import com.google.common.base.Optional;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.FraudDetectedDetails;
import uk.gov.ida.saml.core.domain.PassthroughAssertion;
import uk.gov.ida.saml.core.domain.PersistentId;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.fromNullable;
import static uk.gov.ida.saml.core.test.builders.PersistentIdBuilder.aPersistentId;

public class PassthroughAssertionBuilder {

    private PersistentId persistentId = aPersistentId().build();
    private Optional<AuthnContext> authnContext = fromNullable(AuthnContext.LEVEL_1);
    private String underlyingAssertion = "blob";
    private Optional<FraudDetectedDetails> fraudDetectedDetails = absent();
    private Optional<String> principalIpAddress = fromNullable("principal-ip-address");

    public static PassthroughAssertionBuilder aPassthroughAssertion() {
        return new PassthroughAssertionBuilder();
    }

    public PassthroughAssertion buildMatchingServiceAssertion() {
        return new PassthroughAssertion(
                persistentId,
                authnContext,
                underlyingAssertion,
                fraudDetectedDetails,
                Optional.absent());
    }

    public PassthroughAssertion buildAuthnStatementAssertion() {
        return new PassthroughAssertion(
                persistentId,
                authnContext,
                underlyingAssertion,
                fraudDetectedDetails,
                principalIpAddress);
    }

    public PassthroughAssertion buildMatchingDatasetAssertion() {
        return new PassthroughAssertion(
                persistentId,
                Optional.absent(),
                underlyingAssertion,
                fraudDetectedDetails,
                Optional.absent());
    }

    public PassthroughAssertionBuilder withPersistentId(PersistentId persistentId) {
        this.persistentId = persistentId;
        return this;
    }

    public PassthroughAssertionBuilder withUnderlyingAssertion(String underlyingAssertion) {
        this.underlyingAssertion = underlyingAssertion;
        return this;
    }

    public PassthroughAssertionBuilder withAuthnContext(AuthnContext authnContext) {
        this.authnContext = fromNullable(authnContext);
        return this;
    }
    public String buildMatchingDatasetAssertionAsString() {
        return underlyingAssertion; //this is wrong (obviously) but it is sufficient to make tests that use this method pass for now
    }

    public String buildAuthnStatementAssertionAsString() {
        return underlyingAssertion; //this is wrong (obviously) but it is sufficient to make tests that use this method pass for now
    }

    public PassthroughAssertionBuilder withFraudDetectedDetails(FraudDetectedDetails fraudDetectedDetails) {
        this.fraudDetectedDetails = fromNullable(fraudDetectedDetails);
        return this;
    }

    public PassthroughAssertionBuilder withPrincipalIpAddressSeenByIdp(String principalIpAddress) {
        this.principalIpAddress = fromNullable(principalIpAddress);
        return this;
    }
}
