package uk.gov.ida.integrationTest.support;

import com.google.common.collect.ImmutableList;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import helpers.ManagedFileResource;
import helpers.TemporaryFileResource;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.saml.core.test.TestCertificateStrings;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;

import static helpers.TemporaryFileResourceBuilder.aTemporaryFileResource;
import static java.util.Arrays.asList;
import static org.apache.commons.codec.binary.Base64.decodeBase64;

public class TestRpAppRule extends DropwizardAppRule<TestRpConfiguration> {
    private final List<ManagedFileResource> managedFileResources;

    private TestRpAppRule(final List<ManagedFileResource> managedFileResources, final ConfigOverride[] configOverrides) {
        super(TestRpIntegrationApplication.class, ResourceHelpers.resourceFilePath("test-rp.yml"), configOverrides);
        this.managedFileResources = managedFileResources;
    }

    public static TestRpAppRule newTestRpAppRule(final ConfigOverride... configOverrides) {
        TemporaryFileResource privateSigningKey = aTemporaryFileResource()
                .content(decodeBase64(TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY))
                .build();
        TemporaryFileResource publicSigningCert = aTemporaryFileResource()
                .content(TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT)
                .build();
        TemporaryFileResource privateEncryptionKey = aTemporaryFileResource()
                .content(decodeBase64(TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY))
                .build();
        TemporaryFileResource publicEncryptionCert = aTemporaryFileResource()
                .content(TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT)
                .build();

        List<ManagedFileResource> managedFileResources = asList(
                privateSigningKey,
                publicSigningCert,
                privateEncryptionKey,
                publicEncryptionCert
        );

        ImmutableList<ConfigOverride> mergedConfigOverrides = ImmutableList.<ConfigOverride>builder()
                .add(ConfigOverride.config("privateSigningKeyConfiguration.keyFile", privateSigningKey.getPath()))
                .add(ConfigOverride.config("publicSigningCert.certFile", publicSigningCert.getPath() ))
                .add(ConfigOverride.config("privateEncryptionKeyConfiguration.keyFile", privateEncryptionKey.getPath()))
                .add(ConfigOverride.config("publicEncryptionCert.certFile", publicEncryptionCert.getPath()))
                .add(configOverrides)
                .build();

        JerseyGuiceUtils.reset();
        ConfigOverride[] configOverridesArray = mergedConfigOverrides.toArray(new ConfigOverride[mergedConfigOverrides.size()]);
        return new TestRpAppRule(managedFileResources, configOverridesArray);
    }

    @Override
    protected void before() {
        managedFileResources.forEach(ManagedFileResource::create);
        super.before();
    }

    @Override
    protected void after() {
        managedFileResources.forEach(ManagedFileResource::delete);
        super.after();
    }

    public UriBuilder uriBuilder (String path) {
        return UriBuilder.fromUri("http://localhost")
            .path(path)
            .port(getLocalPort());
    }

    public URI uri(String path) {
        return uriBuilder(path).build();
    }
}
