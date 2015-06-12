package pl.devoxx.aggregatr.aggregation;

import org.springframework.scheduling.annotation.Async;
import pl.devoxx.aggregatr.aggregation.model.Ingredient;
import rx.Observable;

class IngredientsWorker {

    private final IngredientsAggregator ingredientsAggregator;

    IngredientsWorker(IngredientsAggregator ingredientsAggregator) {
        this.ingredientsAggregator = ingredientsAggregator;
    }

    @Async
    void dispatchIngredients() {
        Observable<Ingredient> ingredients = ingredientsAggregator.fetchIngredients();

    }

}
