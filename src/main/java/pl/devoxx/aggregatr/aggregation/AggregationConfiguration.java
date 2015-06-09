package pl.devoxx.aggregatr.aggregation;

import com.nurkiewicz.asyncretry.RetryExecutor;
import com.ofg.infrastructure.web.resttemplate.fluent.ServiceRestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AggregationConfiguration {
    @Bean
    IngredientsAggregator ingredientsAggregator(IngredientHarvester ingredientHarvester) {
        return new IngredientsAggregator(ingredientHarvester);
    }

    @Bean
    IngredientHarvester ingredientHarvester(ServiceRestClient serviceRestClient, RetryExecutor retryExecutor) {
        return new IngredientHarvester(serviceRestClient, retryExecutor);
    }
}

