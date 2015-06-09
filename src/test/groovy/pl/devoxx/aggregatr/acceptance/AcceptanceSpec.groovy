package pl.devoxx.aggregatr.acceptance

import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import pl.devoxx.aggregatr.base.MicroserviceMvcWiremockSpec

import static java.net.URI.create
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@ContextConfiguration
class AcceptanceSpec extends MicroserviceMvcWiremockSpec {

    def 'should call in parallel chmieleo.pl, słodeo.pl, wodeo.pl, drożdzeo.pl to aggregate ingredients'() {
        given: 'a request'
        when: 'getting /ingredients'
            MvcResult result = mockMvc.perform(get(create('/ingredients'))).andReturn()
        then: 'aggregated ingredients will be presented'
            result.response.contentAsString
    }

}
