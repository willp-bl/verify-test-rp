package uk.gov.ida.rp.testrp.metadata;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Throwables;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import io.dropwizard.testing.ResourceHelpers;
import org.apache.commons.io.FileUtils;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import uk.gov.ida.rp.testrp.MsaStubRule;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.exceptions.InsecureMetadataException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.File;
import java.net.URI;
import java.security.KeyStore;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetadataResolverProviderTest {

    @BeforeClass
    public static void doALittleHackToMakeGuicierHappyForSomeReason() {
        JerseyGuiceUtils.reset();
    }

    @ClassRule
    public static WireMockRule msaStubRule = MsaStubRule.create("metadata.xml");

    @Mock
    TestRpConfiguration configuration;

    private static Client client = ClientBuilder.newBuilder().hostnameVerifier(new NoopHostnameVerifier()).trustStore(createKeyStore()).build();

    @Test
    public void shouldPerformHttpsRequestWhenInsecureMetadataFlagIsNotPresent() throws Exception {
        when(configuration.getMsaMetadataUri()).thenReturn(URI.create("https://localhost:6663/metadata"));

        MetadataResolverProvider provider = new MetadataResolverProvider(client, configuration);

        assertCanQueryMetadata(provider);
    }

    @Test
    public void shouldPerformHttpsRequestWhenInsecureMetadataFlagIsTrue() throws Exception {
        when(configuration.getMsaMetadataUri()).thenReturn(URI.create("https://localhost:6663/metadata"));
        when(configuration.getAllowInsecureMetadataLocation()).thenReturn(true);

        MetadataResolverProvider provider = new MetadataResolverProvider(client, configuration);

        assertCanQueryMetadata(provider);
    }

    @Test(expected = InsecureMetadataException.class)
    public void shouldThrowExceptionWhenPerformingHttpRequestWhenInsecureMetadataFlagIsFalse() throws Exception {
        when(configuration.getMsaMetadataUri()).thenReturn(URI.create("http://localhost:5555/metadata"));
        when(configuration.getAllowInsecureMetadataLocation()).thenReturn(false);

        MetadataResolverProvider provider = new MetadataResolverProvider(client, configuration);

        provider.get();
    }

    @Test
    public void shouldPerformHttpRequestWhenInsecureMetadataFlagIsTrue() throws Exception {
        when(configuration.getMsaMetadataUri()).thenReturn(URI.create("http://localhost:5555/metadata"));
        when(configuration.getAllowInsecureMetadataLocation()).thenReturn(true);

        MetadataResolverProvider provider = new MetadataResolverProvider(client, configuration);

        assertCanQueryMetadata(provider);
    }

    private void assertCanQueryMetadata(MetadataResolverProvider provider) throws net.shibboleth.utilities.java.support.resolver.ResolverException {
        CriteriaSet criteria = new CriteriaSet(new EntityIdCriterion("http://www.test-rp-ms.gov.uk/SAML2/MD"));
        EntityDescriptor entityDescriptor = provider.get().resolveSingle(criteria);
        assertNotNull(entityDescriptor);
    }


    private static KeyStore createKeyStore() {
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("JKS");
            ks.load(FileUtils.openInputStream(new File("test_keys/dev_service_ssl.ks")), "marshmallow".toCharArray());
        } catch (Exception e) {
            Throwables.propagate(e);
        }
        return ks;
    }
}
