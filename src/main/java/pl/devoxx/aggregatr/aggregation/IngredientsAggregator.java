package pl.devoxx.aggregatr.aggregation;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.nurkiewicz.asyncretry.RetryExecutor;
import com.ofg.infrastructure.web.resttemplate.fluent.ServiceRestClient;
import lombok.extern.slf4j.Slf4j;
import pl.devoxx.aggregatr.aggregation.model.Ingredient;
import pl.devoxx.aggregatr.aggregation.model.IngredientType;
import pl.devoxx.aggregatr.aggregation.model.Ingredients;
import pl.devoxx.aggregatr.aggregation.model.Order;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
class IngredientsAggregator {

    private static final Cache<IngredientType, Integer> DATABASE = CacheBuilder.newBuilder().build();

    private final IngredientsHarvester ingredientsHarvester;
    private final IngredientsProperties ingredientsProperties;
    private final Map<IngredientType, Meter> meters;
    private final DojrzewatrUpdater dojrzewatrUpdater;

    IngredientsAggregator(ServiceRestClient serviceRestClient,
                          RetryExecutor retryExecutor,
                          IngredientsProperties ingredientsProperties,
                          MetricRegistry metricRegistry) {
        this.ingredientsHarvester = new IngredientsHarvester(serviceRestClient, retryExecutor, ingredientsProperties);
        this.dojrzewatrUpdater = new DojrzewatrUpdater(serviceRestClient, retryExecutor, ingredientsProperties, DATABASE);
        this.ingredientsProperties = ingredientsProperties;
        this.meters = ImmutableMap.of(
                IngredientType.WATER, metricRegistry.meter(getMetricName(IngredientType.WATER)),
                IngredientType.HOP, metricRegistry.meter(getMetricName(IngredientType.HOP)),
                IngredientType.MALT, metricRegistry.meter(getMetricName(IngredientType.MALT)),
                IngredientType.YIEST, metricRegistry.meter(getMetricName(IngredientType.YIEST))
        );
    }

    private String getMetricName(IngredientType ingredientType) {
        return "ingredients." + ingredientType.toString().toLowerCase();
    }

    Ingredients fetchIngredients(Order order) {
        List<ListenableFuture<Ingredient>> futures = ingredientsProperties
                .getListOfServiceNames(order)
                .stream()
                .map(ingredientsHarvester::harvest)
                .collect(Collectors.toList());
        ListenableFuture<List<Ingredient>> allDoneFuture = Futures.allAsList(futures);
        List<Ingredient> allIngredients = Futures.getUnchecked(allDoneFuture);
        allIngredients.forEach(this::updateIngredientCache);
        Ingredients ingredients = getIngredientsStatus();
        dojrzewatrUpdater.updateIfLimitReached(ingredients);
        return ingredients;
    }

    private void updateIngredientCache(Ingredient ingredient) {
        int quantity = Optional.ofNullable(DATABASE.getIfPresent(ingredient.type)).orElse(0);
        DATABASE.put(ingredient.type, quantity + ingredient.quantity);
        meters.get(ingredient.type).mark(ingredient.quantity);
    }

    private Ingredients getIngredientsStatus() {
        return new Ingredients(DATABASE.getAllPresent(Arrays.asList(IngredientType.values()))
                .entrySet()
                .stream()
                .map((entry) -> new Ingredient(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList()));
    }

}
