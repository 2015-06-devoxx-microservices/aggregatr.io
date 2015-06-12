package pl.devoxx.aggregatr.acceptance

import groovy.json.JsonSlurper
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MvcResult
import pl.devoxx.aggregatr.base.MicroserviceMvcWiremockSpec

import static java.net.URI.create
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print

@ContextConfiguration
class AcceptanceSpec extends MicroserviceMvcWiremockSpec {

    def 'should call in parallel chmieleo.pl, słodeo.pl, wodeo.pl, drożdzeo.pl to aggregate ingredients'() {
        given: 'a request'
        when: 'getting /ingredients'
            MvcResult result = mockMvc.perform(get(create('/ingredients'))).andDo(print()).andReturn()
        then: 'aggregated ingredients will be presented'
            !result.resolvedException
            new JsonSlurper().parseText(result.response.contentAsString) == new JsonSlurper().parseText('''
                { "ingredients": [{"type":"HOP","quantity":50},{"type":"MALT","quantity":200},{"type":"WATER","quantity":1000},{"type":"YIEST","quantity":100}]}
            ''')
    }

}
