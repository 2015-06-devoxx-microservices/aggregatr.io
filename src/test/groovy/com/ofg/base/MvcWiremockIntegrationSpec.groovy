package com.ofg.base

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import com.ofg.config.web.HttpMockServer
import groovy.transform.TypeChecked
import org.springframework.beans.factory.annotation.Autowired

@TypeChecked
class MvcWiremockIntegrationSpec extends MvcIntegrationSpec {
    protected WireMock colaWireMock
    @Autowired HttpMockServer httpMockServer

    void setup() {
        colaWireMock = new WireMock('localhost', httpMockServer.port())
        colaWireMock.resetMappings()
    }

    protected void stubInteraction(MappingBuilder mapping, ResponseDefinitionBuilder response) {
        colaWireMock.register(mapping.willReturn(response))
    }
}
