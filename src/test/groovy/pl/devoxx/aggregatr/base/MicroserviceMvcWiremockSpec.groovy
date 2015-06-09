package pl.devoxx.aggregatr.base
import com.ofg.infrastructure.base.MvcWiremockIntegrationSpec
import com.ofg.infrastructure.discovery.ServiceConfigurationResolver
import com.ofg.infrastructure.discovery.web.HttpMockServer
import com.ofg.infrastructure.stub.Stubs
import com.ofg.infrastructure.web.correlationid.CorrelationIdFilter
import com.ofg.stub.StubRunning
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.setup.ConfigurableMockMvcBuilder
import pl.devoxx.aggregatr.Application

@ContextConfiguration(classes = [Application, Config], loader = SpringApplicationContextLoader, inheritLocations = false)
class MicroserviceMvcWiremockSpec extends MvcWiremockIntegrationSpec {

    @Autowired HttpMockServer httpMockServer

    @Override
    protected void configureMockMvcBuilder(ConfigurableMockMvcBuilder mockMvcBuilder) {
        super.configureMockMvcBuilder(mockMvcBuilder)
        mockMvcBuilder.addFilter(new CorrelationIdFilter())
    }

    @Configuration
    static class Config {

        @Bean(destroyMethod = 'shutdownServer', initMethod = "start")
        HttpMockServer httpMockServer() {
            return new HttpMockServer()
        }

        @Bean
        Stubs stubs(ServiceConfigurationResolver serviceConfigurationResolver, StubRunning stubRunning) {
            return new Stubs(serviceConfigurationResolver, stubRunning)
        }

    }
}
