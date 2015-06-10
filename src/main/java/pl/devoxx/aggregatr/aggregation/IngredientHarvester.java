package pl.devoxx.aggregatr.aggregation;

import com.google.common.util.concurrent.ListenableFuture;
import com.nurkiewicz.asyncretry.RetryExecutor;
import com.ofg.infrastructure.web.resttemplate.fluent.ServiceRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.devoxx.aggregatr.model.Ingredient;

import java.lang.invoke.MethodHandles;

import static com.netflix.hystrix.HystrixCommand.Setter.withGroupKey;
import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey;

class IngredientHarvester {

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
