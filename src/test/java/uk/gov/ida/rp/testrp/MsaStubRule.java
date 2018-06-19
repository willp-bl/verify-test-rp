package uk.gov.ida.rp.testrp;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.dropwizard.testing.ResourceHelpers;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class MsaStubRule {

    private final WireMockServer server;

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop();
    }

    public MsaStubRule(String metadataFilename) {
        server = new WireMockServer(wireMockConfig()
                .dynamicHttpsPort()
                .dynamicPort()
                .keystorePath("test_keys/dev_service_ssl.ks")
                .keystorePassword("marshmallow"));

        String metadataContent;
        try {
            metadataContent = FileUtils.readFileToString(new File(ResourceHelpers.resourceFilePath(metadataFilename)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.stubFor(WireMock.get(urlEqualTo("/metadata"))
                .willReturn(aResponse()
                        .withBody(metadataContent)));
        start();
    }

    public int getPort() {
        start();
        return server.port();
    }

    public int getSecurePort() {
        start();
        return server.httpsPort();
    }
}
