package com.ofg.microservice.config.web
import com.ofg.microservice.infrastructure.correlationid.CorrelationIdFilter
import groovy.transform.TypeChecked
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.embedded.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import javax.servlet.Filter

@Configuration
@TypeChecked
class WebConfiguration {
    @Bean
    Filter requestBodyLoggingContextFilter(@Value('${request.payload.logging.maxlength:2000}') int maxPayloadLength) {
        return new RequestBodyLoggingContextFilter(maxPayloadLength)
    }

    @Bean
    FilterRegistrationBean correlationHeaderFilter() {
        return new FilterRegistrationBean(new CorrelationIdFilter())
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate()
    }

    @Bean
    Filter apiDocsCORSFilter() {
        return new CORSFilter()
    }
}
