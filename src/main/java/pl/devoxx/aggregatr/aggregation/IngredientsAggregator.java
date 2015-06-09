package pl.devoxx.aggregatr.aggregation;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import org.springframework.scheduling.annotation.Async;
import pl.devoxx.aggregatr.model.Ingredients;
import rx.Observable;

import java.util.Arrays;
import java.util.List;

class IngredientsAggregator {

    private final IngredientHarvester ingredientHarvester;

    IngredientsAggregator(IngredientHarvester ingredientHarvester) {
        this.ingredientHarvester = ingredientHarvester;
    }

    @Async
    List<Ingredients> fetchIngredients() {
        List<Ingredients> listOfRetrievedIngredients = Lists.newArrayList();
        Observable.from(Arrays.asList("chmieleo", "słodeo", "wodeo", "drożdzeo"))
                .map(ingredientHarvester::harvest)
                .map(this::getSafely)
                .forEach((listOfRetrievedIngredients::addAll));
        return listOfRetrievedIngredients;
    }


    private List<Ingredients> getSafely(ListenableFuture<List<Ingredients>> list) {
        try {
            return list.get();
        } catch (Exception e) {
            return null;
        }
    }

}
