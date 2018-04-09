package uk.gov.ida.rp.testrp.saml.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;

public class SamlConfiguration {
    protected SamlConfiguration() {
    }

    @Valid
    @NotNull
    @JsonProperty
    protected String entityId;

    @Valid
    @JsonProperty
    protected URI expectedDestination = URI.create("http://configure.me/if/i/fail");

    public URI getExpectedDestinationHost() {
        return expectedDestination;
    }

    public String getEntityId() {
        return entityId;
    }
}
