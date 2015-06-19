package pl.devoxx.aggregatr.aggregation;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
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
    private final Map<IngredientType, Metric> meters;
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
                IngredientType.WATER, metricRegistry.register(getMetricName(IngredientType.WATER),
                        (Gauge<Integer>) () -> ingredientWarehouse.getIngredientCountOfType(IngredientType.WATER)),
                IngredientType.HOP, metricRegistry.register(getMetricName(IngredientType.HOP),
                        (Gauge<Integer>) () -> ingredientWarehouse.getIngredientCountOfType(IngredientType.HOP)),
                IngredientType.MALT, metricRegistry.register(getMetricName(IngredientType.MALT),
                        (Gauge<Integer>) () -> ingredientWarehouse.getIngredientCountOfType(IngredientType.MALT)),
                IngredientType.YIEST, metricRegistry.register(getMetricName(IngredientType.YIEST),
                        (Gauge<Integer>) () -> ingredientWarehouse.getIngredientCountOfType(IngredientType.YIEST))
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
        allIngredients.stream()
                .filter((ingredient -> ingredient != null))
                .forEach(this::updateIngredientCache);
        Ingredients ingredients = getIngredientsStatus();
        return dojrzewatrUpdater.updateIfLimitReached(ingredients);
    }

    private void updateIngredientCache(Ingredient ingredient) {
        ingredientWarehouse.addIngredient(ingredient);
    }

    private Ingredients getIngredientsStatus() {
        return ingredientWarehouse.getCurrentState();
    }

}
