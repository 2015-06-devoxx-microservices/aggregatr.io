package pl.devoxx.aggregatr.aggregation;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.ListenableFuture;
import com.nurkiewicz.asyncretry.RetryExecutor;
import com.ofg.infrastructure.web.resttemplate.fluent.ServiceRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import pl.devoxx.aggregatr.aggregation.model.Ingredient;
import pl.devoxx.aggregatr.aggregation.model.IngredientType;
import pl.devoxx.aggregatr.aggregation.model.Ingredients;
import rx.Observable;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.netflix.hystrix.HystrixCommand.Setter.withGroupKey;
import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey;

class IngredientsAggregator {

    private final IngredientHarvester ingredientHarvester;
    private final IngredientsProperties ingredientsProperties;
    private static final Cache<IngredientType, Integer> DATABASE = CacheBuilder.newBuilder().recordStats().build();

    IngredientsAggregator(ServiceRestClient serviceRestClient,
                          RetryExecutor retryExecutor,
                          IngredientsProperties ingredientsProperties) {
        this.ingredientHarvester = new IngredientHarvester(serviceRestClient, retryExecutor, ingredientsProperties);
        this.ingredientsProperties = ingredientsProperties;
    }

    @Async
    Ingredients fetchIngredients() {
        Observable.from(ingredientsProperties.getListOfServiceNames())
                .map(ingredientHarvester::harvest)
                .flatMap(Observable::from)
                .forEach(ingredient -> {
                    int quantity = Optional.ofNullable(DATABASE.getIfPresent(ingredient.type)).orElse(0);
                    DATABASE.put(ingredient.type, quantity + ingredient.quantity);
                });
        return getIngredientsStatus();
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
