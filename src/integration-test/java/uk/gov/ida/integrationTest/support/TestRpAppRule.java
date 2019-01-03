package uk.gov.ida.integrationTest.support;

import certificates.values.CACertificates;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import keystore.CertificateEntry;
import keystore.KeyStoreResource;
import keystore.builders.KeyStoreResourceBuilder;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.rp.testrp.MsaStubRule;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.saml.core.test.TestCertificateStrings;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class TestRpAppRule extends DropwizardAppRule<TestRpConfiguration> {

    private TestRpAppRule(final ConfigOverride[] configOverrides) {
        super(TestRpIntegrationApplication.class, ResourceHelpers.resourceFilePath("test-rp.yml"), configOverrides);
    }

    private static KeyStoreResource trustStore;

    static {
        trustStore = KeyStoreResourceBuilder.aKeyStoreResource()
                .withCertificates(ImmutableList.of(new CertificateEntry("test_root_ca", CACertificates.TEST_ROOT_CA),
                        new CertificateEntry("test_rp_ca", CACertificates.TEST_RP_CA)))
                .build();
        trustStore.create();
    }

    public static TestRpAppRule newTestRpAppRule(final ConfigOverride... configOverrides) {
        return newTestRpAppRule(new MsaStubRule(), configOverrides);
    }

    public static TestRpAppRule newTestRpAppRule(MsaStubRule msaStubRule, final ConfigOverride... configOverrides) {
        try {
            msaStubRule.setUpRegularMetadata();
        } catch (JsonProcessingException | MarshallingException | SignatureException e) {
            Throwables.propagate(e);
        }
        ImmutableList<ConfigOverride> mergedConfigOverrides = ImmutableList.<ConfigOverride>builder()
                .add(ConfigOverride.config("server.applicationConnectors[0].port", "0"))
                .add(ConfigOverride.config("server.adminConnectors[0].port", "0"))
                .add(ConfigOverride.config("msaEntityId", msaStubRule.METADATA_ENTITY_ID))
                .add(ConfigOverride.config("msaMetadataUri", msaStubRule.METADATA_ENTITY_ID))
                .add(ConfigOverride.config("msaMetadataTrustStoreConfiguration.path", trustStore.getAbsolutePath()))
                .add(ConfigOverride.config("msaMetadataTrustStoreConfiguration.password", trustStore.getPassword()))
                .add(ConfigOverride.config("allowInsecureMetadataLocation", "true"))
                .add(ConfigOverride.config("clientTrustStoreConfiguration.path", ResourceHelpers.resourceFilePath("ida_truststore.ts")))
                .add(ConfigOverride.config("privateSigningKeyConfiguration.key", TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY))
                .add(ConfigOverride.config("privateSigningKeyConfiguration.type", "encoded"))
                .add(ConfigOverride.config("publicSigningCert.cert", TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT))
                .add(ConfigOverride.config("publicSigningCert.type", "x509"))
                .add(ConfigOverride.config("privateEncryptionKeyConfiguration.key", TestCertificateStrings.TEST_RP_PRIVATE_ENCRYPTION_KEY))
                .add(ConfigOverride.config("privateEncryptionKeyConfiguration.type", "encoded"))
                .add(ConfigOverride.config("publicEncryptionCert.cert", TestCertificateStrings.TEST_RP_PUBLIC_ENCRYPTION_CERT))
                .add(ConfigOverride.config("publicEncryptionCert.type", "x509"))
                .add(configOverrides)
                .build();

        JerseyGuiceUtils.reset();
        ConfigOverride[] configOverridesArray = mergedConfigOverrides.toArray(new ConfigOverride[mergedConfigOverrides.size()]);
        return new TestRpAppRule(configOverridesArray);
    }

    @Override
    protected void before() {
        super.before();
    }

    @Override
    protected void after() {
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
