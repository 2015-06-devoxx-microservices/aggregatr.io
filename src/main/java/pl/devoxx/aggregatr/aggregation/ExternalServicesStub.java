package pl.devoxx.aggregatr.aggregation;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;

class ExternalServicesStub implements Closeable {

    private final WireMockServer wireMockServer;

    ExternalServicesStub(IngredientsProperties ingredientsProperties) throws IOException {
        URI uri = URI.create(ingredientsProperties.getRootUrl());
        this.wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig()
                .port(uri.getPort()));
        wireMockServer.addStubMapping(StubMapping.buildFrom(IOUtils.toString(ExternalServicesStub.class.getResourceAsStream("/mappings/chmieleo.json"))));
        wireMockServer.addStubMapping(StubMapping.buildFrom(IOUtils.toString(ExternalServicesStub.class.getResourceAsStream("/mappings/drozdzeo.json"))));
        wireMockServer.addStubMapping(StubMapping.buildFrom(IOUtils.toString(ExternalServicesStub.class.getResourceAsStream("/mappings/slodeo.json"))));
        wireMockServer.addStubMapping(StubMapping.buildFrom(IOUtils.toString(ExternalServicesStub.class.getResourceAsStream("/mappings/wodeo.json"))));
    }

    void start() {
        wireMockServer.start();
    }

    @Override
    public void close() throws IOException {
        wireMockServer.stop();
    }
}
