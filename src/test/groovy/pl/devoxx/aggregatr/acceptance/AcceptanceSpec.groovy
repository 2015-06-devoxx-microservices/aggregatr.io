package pl.devoxx.aggregatr.acceptance

import groovy.json.JsonSlurper
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MvcResult
import pl.devoxx.aggregatr.aggregation.model.Version
import pl.devoxx.aggregatr.base.MicroserviceMvcWiremockSpec
import spock.lang.Ignore

import static java.net.URI.create
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print

@ContextConfiguration
@ActiveProfiles(['test', 'offline'])
@Ignore
class AcceptanceSpec extends MicroserviceMvcWiremockSpec {

    def 'should return empty warehouse after retrieving threshold amount of ingredients from external services'() {
        when:
        MvcResult result = getting_ingredients()
        then:
        aggregated_ingredients_are_present(result)
    }

    private MvcResult getting_ingredients() {
        return mockMvc.perform(post(create('/ingredients'))
                .header('Content-Type', Version.AGGREGATOR_V1)
                .content('{"items":["WATER","HOP","YIEST","MALT"]}'))
                .andDo(print())
                .andReturn()
    }

    private void aggregated_ingredients_are_present(MvcResult result) {
        assert !result.resolvedException
        Map parsedResult = new JsonSlurper().parseText(result.response.contentAsString)
        Map expectedResult = new JsonSlurper().parseText('''
                {
                    "ingredients": [
                            {"type":"MALT","quantity":0},
                            {"type":"WATER","quantity":0},
                            {"type":"HOP","quantity":0},
                            {"type":"YIEST","quantity":0}
                        ]
                }
            ''')
        def sort = { a, b -> a.type.compareTo(b.type) }
        assert parsedResult.ingredients.sort(sort) == expectedResult.ingredients.sort(sort)
    }

}
