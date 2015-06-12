package pl.devoxx.aggregatr.aggregation;

import com.nurkiewicz.asyncretry.RetryExecutor;
import com.ofg.infrastructure.web.resttemplate.fluent.ServiceRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
class AggregationConfiguration {
    @Bean
    IngredientsAggregator ingredientsAggregator(ServiceRestClient serviceRestClient, RetryExecutor retryExecutor, @Value("${listOfUrls}") String listOfUrls) {
        return new IngredientsAggregator(serviceRestClient, retryExecutor, Arrays.asList(listOfUrls.split(",")));
    }
}

