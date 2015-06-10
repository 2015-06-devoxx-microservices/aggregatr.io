package pl.devoxx.aggregatr.aggregation;

import com.google.common.util.concurrent.ListenableFuture;
import org.springframework.scheduling.annotation.Async;
import pl.devoxx.aggregatr.model.Ingredient;
import rx.Observable;

import java.util.Arrays;

class IngredientsAggregator {

    private final IngredientHarvester ingredientHarvester;

    IngredientsAggregator(IngredientHarvester ingredientHarvester) {
        this.ingredientHarvester = ingredientHarvester;
    }

    @Async
    Observable<Ingredient> fetchIngredients() {
        return Observable.from(Arrays.asList("chmieleo", "słodeo", "wodeo", "drożdzeo"))
                .map(ingredientHarvester::harvest)
                .flatMap((ListenableFuture<Ingredient> future) -> Observable.from(future));
    }

}
