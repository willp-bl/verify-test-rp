package uk.gov.ida.rp.testrp.metadata;

import org.apache.log4j.Logger;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import uk.gov.ida.jerseyclient.JerseyClientConfigurationBuilder;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.saml.metadata.MetadataConfiguration;
import uk.gov.ida.saml.metadata.factories.DropwizardMetadataResolverFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.client.Client;
import java.security.KeyStore;
import java.util.Objects;

import static java.text.MessageFormat.format;

public class MetadataResolverProvider implements Provider<MetadataResolver> {

    private static final Logger LOG = Logger.getLogger(MetadataResolverProvider.class);

    private final MetadataResolver metadataResolver;

    @Inject
    public MetadataResolverProvider(Client client, TestRpConfiguration configuration) {
        MetadataConfiguration metadataConfiguration = new MetadataConfiguration(configuration.getMsaMetadataUri(),
                Long.valueOf(2000),
                Long.valueOf(600000),
                configuration.getMsaEntityId(),
                JerseyClientConfigurationBuilder.aJerseyClientConfiguration().build(),
                "metadataProvider",
                ""
        ) {
            @Override
            public KeyStore getTrustStore() {
                return Objects.isNull(configuration.getMsaMetadataTrustStoreConfiguration())?
                        null
                        :configuration.getMsaMetadataTrustStoreConfiguration().getTrustStore();
            }
        };
        LOG.info(format("Expecting signed metadata: {0}", Objects.nonNull(configuration.getMsaMetadataTrustStoreConfiguration())));
        this.metadataResolver = new DropwizardMetadataResolverFactory().createMetadataResolverWithClient(metadataConfiguration, Objects.nonNull(configuration.getMsaMetadataTrustStoreConfiguration()), client);
    }

    @Override
    public MetadataResolver get() {
        return metadataResolver;
    }

}
