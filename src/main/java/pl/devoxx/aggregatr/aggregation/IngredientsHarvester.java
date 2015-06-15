package pl.devoxx.aggregatr.aggregation;

import com.google.common.util.concurrent.ListenableFuture;
import com.nurkiewicz.asyncretry.RetryExecutor;
import com.ofg.infrastructure.web.resttemplate.fluent.ServiceRestClient;
import lombok.extern.slf4j.Slf4j;
import pl.devoxx.aggregatr.aggregation.model.Ingredient;

import static com.netflix.hystrix.HystrixCommand.Setter.withGroupKey;
import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey;

@Slf4j
class IngredientsHarvester {

    private final ServiceRestClient serviceRestClient;
    private final RetryExecutor retryExecutor;
    private IngredientsProperties ingredientsProperties;

    IngredientsHarvester(ServiceRestClient serviceRestClient, RetryExecutor retryExecutor, IngredientsProperties ingredientsProperties) {
        this.serviceRestClient = serviceRestClient;
        this.retryExecutor = retryExecutor;
        this.ingredientsProperties = ingredientsProperties;
    }

    ListenableFuture<Ingredient> harvest(String url) {
        return serviceRestClient.forExternalService()
                .retryUsing(retryExecutor)
                .get()
                .withCircuitBreaker(withGroupKey(asKey(url)), () -> {
                    log.error("Can't connect to {}", url);
                    return "";
                })
                .onUrl(ingredientsProperties.getRootUrl() + url)
                .andExecuteFor()
                .anObject()
                .ofTypeAsync(Ingredient.class);
    }

}