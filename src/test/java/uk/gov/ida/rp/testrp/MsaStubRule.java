package uk.gov.ida.rp.testrp;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.dropwizard.testing.ResourceHelpers;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class MsaStubRule {

    public static WireMockRule create(String metadataFilename) {
        String metadataContent;
        try {
            metadataContent = FileUtils.readFileToString(new File(ResourceHelpers.resourceFilePath(metadataFilename)));
        } catch (IOException e) {
            return null;
        }
        WireMockRule rule = new WireMockRule(WireMockConfiguration.options()
                .port(5555)
                .httpsPort(6663)
                .keystorePath("test_keys/dev_service_ssl.ks")
                .keystorePassword("marshmallow"));
        rule.stubFor(WireMock.get(urlEqualTo("/metadata"))
                .willReturn(aResponse()
                        .withBody(metadataContent)));
        return rule;
    }
}
