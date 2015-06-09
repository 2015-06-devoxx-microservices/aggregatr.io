package pl.devoxx.aggregatr.aggregation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AggregationConfiguration {
    @Bean
    IngredientsAggregator ingredientsAggregator() {
        return new IngredientsAggregator();
    }
}

