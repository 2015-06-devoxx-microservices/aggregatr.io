package pl.devoxx.aggregatr.aggregation;

import org.springframework.scheduling.annotation.Async;
import pl.devoxx.aggregatr.model.Ingredients;

import java.util.List;

public class IngredientsAggregator {

    @Async
    List<Ingredients> fetchIngredients() {
        return null;
    }

}
