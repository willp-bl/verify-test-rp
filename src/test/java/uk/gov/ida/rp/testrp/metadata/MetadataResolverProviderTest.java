package uk.gov.ida.rp.testrp.metadata;

import certificates.values.CACertificates;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import keystore.CertificateEntry;
import keystore.KeyStoreResource;
import keystore.builders.KeyStoreResourceBuilder;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.rp.testrp.MsaStubRule;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.saml.metadata.TrustStoreConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.net.URI;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetadataResolverProviderTest {

    private static KeyStoreResource msTrustStore;

    static {
        JerseyGuiceUtils.reset();
    }

    public static MsaStubRule msaStubRule = new MsaStubRule();

    @BeforeClass
    public static void createTruststore() {
        msTrustStore = KeyStoreResourceBuilder.aKeyStoreResource()
                .withCertificates(ImmutableList.of(new CertificateEntry("test_root_ca", CACertificates.TEST_ROOT_CA),
                        new CertificateEntry("test_rp_ca", CACertificates.TEST_RP_CA)))
                .build();
        msTrustStore.create();
    }

    @Mock
    TestRpConfiguration configuration;
    @Mock
    private TrustStoreConfiguration trustStoreConfiguration;

    @Before
    public void setUp() throws MarshallingException, SignatureException, JsonProcessingException {
        when(configuration.getClientTrustStoreConfiguration()).thenReturn(trustStoreConfiguration);
        when(trustStoreConfiguration.getTrustStore()).thenReturn(msTrustStore.getKeyStore());
        when(configuration.getMsaEntityId()).thenReturn(msaStubRule.METADATA_ENTITY_ID);
        when(configuration.getMsaMetadataUri()).thenReturn(URI.create(msaStubRule.METADATA_ENTITY_ID));
        msaStubRule.setUpRegularMetadata();
    }

    @Test
    public void shouldRequestMetadata() throws Exception {
        MetadataResolverProvider provider = new MetadataResolverProvider(ClientBuilder.newBuilder().build(), configuration);

        assertCanQueryMetadata(provider);
    }

    private void assertCanQueryMetadata(MetadataResolverProvider provider) throws net.shibboleth.utilities.java.support.resolver.ResolverException {
        CriteriaSet criteria = new CriteriaSet(new EntityIdCriterion(msaStubRule.METADATA_ENTITY_ID));
        EntityDescriptor entityDescriptor = provider.get().resolveSingle(criteria);
        assertNotNull(entityDescriptor);
    }

}
