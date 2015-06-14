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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.devoxx.aggregatr.aggregation.model.Ingredient;
import pl.devoxx.aggregatr.aggregation.model.IngredientType;
import pl.devoxx.aggregatr.aggregation.model.Ingredients;
import pl.devoxx.aggregatr.aggregation.model.Order;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.netflix.hystrix.HystrixCommand.Setter.withGroupKey;
import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey;

class IngredientsAggregator {

    private static final Cache<IngredientType, Integer> DATABASE = CacheBuilder.newBuilder().build();

    private final IngredientHarvester ingredientHarvester;
    private final IngredientsProperties ingredientsProperties;
    private final Map<IngredientType, Meter> meters;

    IngredientsAggregator(ServiceRestClient serviceRestClient,
                          RetryExecutor retryExecutor,
                          IngredientsProperties ingredientsProperties,
                          MetricRegistry metricRegistry) {
        this.ingredientHarvester = new IngredientHarvester(serviceRestClient, retryExecutor, ingredientsProperties);
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
                .map(ingredientHarvester::harvest)
                .collect(Collectors.toList());
        ListenableFuture<List<Ingredient>> allDoneFuture = Futures.allAsList(futures);
        List<Ingredient> allIngredients = Futures.getUnchecked(allDoneFuture);
        allIngredients.forEach(this::updateIngredientCache);
        return getIngredientsStatus();
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

    private static class IngredientHarvester {

        private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

        private final ServiceRestClient serviceRestClient;
        private final RetryExecutor retryExecutor;
        private IngredientsProperties ingredientsProperties;

        IngredientHarvester(ServiceRestClient serviceRestClient, RetryExecutor retryExecutor, IngredientsProperties ingredientsProperties) {
            this.serviceRestClient = serviceRestClient;
            this.retryExecutor = retryExecutor;
            this.ingredientsProperties = ingredientsProperties;
        }

        ListenableFuture<Ingredient> harvest(String url) {
            return serviceRestClient.forExternalService()
                    .retryUsing(retryExecutor)
                    .get()
                    .withCircuitBreaker(withGroupKey(asKey(url)), () -> {
                        LOG.error("Can't connect to {}", url);
                        return "";
                    })
                    .withCircuitBreaker(withGroupKey(asKey(url)))
                    .onUrl(ingredientsProperties.getRootUrl() + url)
                    .andExecuteFor()
                    .anObject()
                    .ofTypeAsync(Ingredient.class);
        }

    }
}
