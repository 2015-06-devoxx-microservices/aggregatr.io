package pl.devoxx.aggregatr.aggregation;

import com.google.common.util.concurrent.ListenableFuture;
import com.nurkiewicz.asyncretry.RetryExecutor;
import com.ofg.infrastructure.web.resttemplate.fluent.ServiceRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import pl.devoxx.aggregatr.aggregation.model.Ingredient;
import rx.Observable;

import java.lang.invoke.MethodHandles;
import java.util.List;

import static com.netflix.hystrix.HystrixCommand.Setter.withGroupKey;
import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey;

class IngredientsAggregator {

    private final IngredientHarvester ingredientHarvester;
    private final List<String> listOfUrls;

    IngredientsAggregator(ServiceRestClient serviceRestClient, RetryExecutor retryExecutor, List<String> listOfUrls) {
        this.ingredientHarvester = new IngredientHarvester(serviceRestClient, retryExecutor);
        this.listOfUrls = listOfUrls;
    }

    @Async
    Observable<Ingredient> fetchIngredients() {
        return Observable.from(listOfUrls)
                .map(ingredientHarvester::harvest)
                .flatMap((ListenableFuture<Ingredient> future) -> Observable.from(future));
    }

    private static class IngredientHarvester {

        private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

        private final ServiceRestClient serviceRestClient;
        private final RetryExecutor retryExecutor;

        IngredientHarvester(ServiceRestClient serviceRestClient, RetryExecutor retryExecutor) {
            this.serviceRestClient = serviceRestClient;
            this.retryExecutor = retryExecutor;
        }

        @SuppressWarnings("unchecked")
        ListenableFuture<Ingredient> harvest(String url) {
            return serviceRestClient.forExternalService()
                    .retryUsing(retryExecutor)
                    .get()
                    .withCircuitBreaker(withGroupKey(asKey(url)), () -> {
                        LOG.error("Can't connect to {}", url);
                        return "";
                    })
                    .withCircuitBreaker(withGroupKey(asKey(url)))
                    .onUrl("http://" + url + ".pl:8030" + "/" + url.substring(0,1))
                    .andExecuteFor()
                    .anObject()
                    .ofTypeAsync(Ingredient.class);
        }

    }
}
