package pl.devoxx.aggregatr.aggregation;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
class IngredientsAggregator {

    private final ExternalCompanyHarvester externalCompanyHarvester;
    private final IngredientsProperties ingredientsProperties;
    private final Map<IngredientType, Meter> meters;
    private final DojrzewatrUpdater dojrzewatrUpdater;
    private final IngredientWarehouse ingredientWarehouse;

    IngredientsAggregator(ServiceRestClient serviceRestClient,
                          RetryExecutor retryExecutor,
                          IngredientsProperties ingredientsProperties,
                          MetricRegistry metricRegistry, IngredientWarehouse ingredientWarehouse) {
        this.ingredientWarehouse = ingredientWarehouse;
        this.externalCompanyHarvester = new ExternalCompanyHarvester(serviceRestClient, retryExecutor, ingredientsProperties);
        this.dojrzewatrUpdater = new DojrzewatrUpdater(serviceRestClient, retryExecutor, ingredientsProperties, ingredientWarehouse);
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
                .map(externalCompanyHarvester::harvest)
                .collect(Collectors.toList());
        ListenableFuture<List<Ingredient>> allDoneFuture = Futures.allAsList(futures);
        List<Ingredient> allIngredients = Futures.getUnchecked(allDoneFuture);
        allIngredients.forEach(this::updateIngredientCache);
        Ingredients ingredients = getIngredientsStatus();
        return dojrzewatrUpdater.updateIfLimitReached(ingredients);
    }

    private void updateIngredientCache(Ingredient ingredient) {
        ingredientWarehouse.addIngredient(ingredient);
        meters.get(ingredient.getType()).mark(ingredient.getQuantity());
    }

    private Ingredients getIngredientsStatus() {
        return ingredientWarehouse.getCurrentState();
    }

}
