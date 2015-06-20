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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.devoxx.aggregatr.aggregation.model.Ingredient;
import pl.devoxx.aggregatr.aggregation.model.IngredientType;
import pl.devoxx.aggregatr.aggregation.model.Ingredients;
import pl.devoxx.aggregatr.aggregation.model.Order;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.netflix.hystrix.HystrixCommand.Setter.withGroupKey;
import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey;

@Slf4j
@Component
class IngredientsAggregator {

    private final IngredientsProperties ingredientsProperties;
    private final ServiceRestClient serviceRestClient;
    private final RetryExecutor retryExecutor;
    private final IngredientWarehouse ingredientWarehouse;

    @Autowired
    IngredientsAggregator(ServiceRestClient serviceRestClient,
                          RetryExecutor retryExecutor,
                          IngredientsProperties ingredientsProperties, IngredientWarehouse ingredientWarehouse) {
        this.serviceRestClient = serviceRestClient;
        this.retryExecutor = retryExecutor;
        this.ingredientWarehouse = ingredientWarehouse;
        this.ingredientsProperties = ingredientsProperties;
    }

    Ingredients fetchIngredients(Order order) {
        List<ListenableFuture<Ingredient>> futures = ingredientsProperties
                .getListOfServiceNames(order)
                .stream()
                .map(this::harvest)
                .collect(Collectors.toList());
        ListenableFuture<List<Ingredient>> allDoneFuture = Futures.allAsList(futures);
        List<Ingredient> allIngredients = Futures.getUnchecked(allDoneFuture);
        allIngredients.stream()
                .filter((ingredient -> ingredient != null))
                .forEach(ingredientWarehouse::addIngredient);
        return ingredientWarehouse.getCurrentState();
    }

    ListenableFuture<Ingredient> harvest(String service) {
        return serviceRestClient.forExternalService()
                .retryUsing(retryExecutor)
                .get()
                .withCircuitBreaker(withGroupKey(asKey(service)), () -> {
                    log.error("Can't connect to {}", service);
                    return null;
                })
                .onUrl(ingredientsProperties.getRootUrl() + "/" + service)
                .andExecuteFor()
                .anObject()
                .ofTypeAsync(Ingredient.class);
    }
}
