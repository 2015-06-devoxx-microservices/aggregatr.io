package pl.devoxx.aggregatr.aggregation;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Service;
import pl.devoxx.aggregatr.aggregation.model.Ingredient;
import pl.devoxx.aggregatr.aggregation.model.IngredientType;
import pl.devoxx.aggregatr.aggregation.model.Ingredients;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
class IngredientWarehouse {

    private static final Cache<IngredientType, Integer> DATABASE = CacheBuilder.newBuilder().build();

    public void addIngredient(Ingredient ingredient) {
        int currentQuantity = getCurrentQuantity(ingredient);
        DATABASE.put(ingredient.getType(), currentQuantity + ingredient.getQuantity());
    }

    public void clearWarehouseByThreshold(Integer threshold) {
        DATABASE.getAllPresent(Arrays.asList(IngredientType.values()))
                .forEach((ingredientType, integer) -> DATABASE.put(ingredientType, integer - threshold));
    }

    private int getCurrentQuantity(Ingredient ingredient) {
        return Optional.ofNullable(DATABASE.getIfPresent(ingredient.getType())).orElse(0);
    }

    public Ingredients getCurrentState() {
        return new Ingredients(DATABASE.getAllPresent(Arrays.asList(IngredientType.values()))
                .entrySet()
                .stream()
                .map((entry) -> new Ingredient(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList()));
    }
}
