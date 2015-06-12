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

    def 'should call external services to aggregate ingredients'() {
        when:
            MvcResult result = getting_ingredients()
        then:
            aggregated_ingredients_are_present(result)
    }

    private MvcResult getting_ingredients() {
        return mockMvc.perform(get(create('/ingredients'))).andDo(print()).andReturn()
    }

    private void aggregated_ingredients_are_present(MvcResult result) {
        assert !result.resolvedException
        Map parsedResult = new JsonSlurper().parseText(result.response.contentAsString)
        Map expectedResult = new JsonSlurper().parseText('''
                {
                    "ingredients": [
                            {"type":"MALT","quantity":200},
                            {"type":"WATER","quantity":1000},
                            {"type":"HOP","quantity":50},
                            {"type":"YIEST","quantity":100}
                        ]
                }
            ''')
       assert parsedResult == expectedResult
    }

}
