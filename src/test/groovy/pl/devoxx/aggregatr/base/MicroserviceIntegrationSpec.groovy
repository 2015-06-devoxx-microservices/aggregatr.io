package pl.devoxx.aggregatr.base

import com.ofg.infrastructure.base.IntegrationSpec
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.test.context.ContextConfiguration
import pl.devoxx.Application
import pl.devoxx.aggregatr.Application

@ContextConfiguration(classes = [Application], loader = SpringApplicationContextLoader)
class MicroserviceIntegrationSpec extends IntegrationSpec {
}
