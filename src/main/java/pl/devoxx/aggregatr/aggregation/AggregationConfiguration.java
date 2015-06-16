package pl.devoxx.aggregatr.aggregation;

import com.codahale.metrics.MetricRegistry;
import com.nurkiewicz.asyncretry.RetryExecutor;
import com.ofg.infrastructure.web.resttemplate.fluent.ServiceRestClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.SocketUtils;

import java.io.IOException;

@Configuration
class AggregationConfiguration {
    @Bean
    IngredientsAggregator ingredientsAggregator(ServiceRestClient serviceRestClient,
                                                RetryExecutor retryExecutor,
                                                MetricRegistry metricRegistry,
                                                IngredientsProperties ingredientsProperties) {
        return new IngredientsAggregator(serviceRestClient, retryExecutor, ingredientsProperties, metricRegistry, ingredientWarehouse());
    }

    @Bean
    IngredientWarehouse ingredientWarehouse() {
        return new IngredientWarehouse();
    }

    @Bean
    IngredientsProperties ingredientsProperties(@Value("${ingredients.rootUrl:}") String rootUrl) {
        IngredientsProperties ingredientsProperties = new IngredientsProperties();
        ingredientsProperties.setRootUrl(StringUtils.defaultIfBlank(rootUrl, "http://localhost:" + String.valueOf(SocketUtils.findAvailableTcpPort())));
        return ingredientsProperties;
    }

    @Bean(initMethod = "start", destroyMethod = "close")
    ExternalServicesStub externalServicesStub(IngredientsProperties  ingredientsProperties) throws IOException {
        return new ExternalServicesStub(ingredientsProperties);
    }
}

