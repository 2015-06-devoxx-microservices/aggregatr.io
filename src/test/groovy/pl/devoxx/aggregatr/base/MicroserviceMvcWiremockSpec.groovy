package pl.devoxx.aggregatr.base

import com.ofg.infrastructure.base.MvcWiremockIntegrationSpec
import com.ofg.infrastructure.web.correlationid.CorrelationIdFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.context.annotation.*
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.setup.ConfigurableMockMvcBuilder
import pl.devoxx.aggregatr.Application
import pl.devoxx.aggregatr.aggregation.ExcludedForIntegrationTests
import pl.devoxx.aggregatr.aggregation.IngredientsProperties

@ContextConfiguration(classes = [Config], loader = SpringApplicationContextLoader)
class MicroserviceMvcWiremockSpec extends MvcWiremockIntegrationSpec {

    @Override
    protected void configureMockMvcBuilder(ConfigurableMockMvcBuilder mockMvcBuilder) {
        super.configureMockMvcBuilder(mockMvcBuilder)
        mockMvcBuilder.addFilter(new CorrelationIdFilter())
    }

    @Configuration
    @ComponentScan(excludeFilters = [@ComponentScan.Filter(value = ExcludedForIntegrationTests, type = FilterType.ANNOTATION)])
    @Import(Application)
    static class Config {

        @Bean
        IngredientsProperties ingredientsProperties(@Value("#{'http://localhost:' + @httpMockServer.port()}") String rootUrl) {
            IngredientsProperties ingredientsProperties = new IngredientsProperties();
            ingredientsProperties.setRootUrl(rootUrl);
            return ingredientsProperties;
        }

    }
}
