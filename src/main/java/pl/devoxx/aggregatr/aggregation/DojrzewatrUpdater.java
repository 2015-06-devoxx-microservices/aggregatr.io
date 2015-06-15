package pl.devoxx.aggregatr.aggregation;

import com.nurkiewicz.asyncretry.RetryExecutor;
import com.ofg.infrastructure.web.resttemplate.fluent.ServiceRestClient;
import lombok.extern.slf4j.Slf4j;
import pl.devoxx.aggregatr.aggregation.model.IngredientType;
import pl.devoxx.aggregatr.aggregation.model.Ingredients;

import static com.netflix.hystrix.HystrixCommand.Setter.withGroupKey;
import static com.netflix.hystrix.HystrixCommandGroupKey.Factory.asKey;

@Slf4j
class DojrzewatrUpdater {

    private final ServiceRestClient serviceRestClient;
    private final RetryExecutor retryExecutor;
    private final IngredientsProperties ingredientsProperties;
    private final IngredientWarehouse ingredientWarehouse;

    public DojrzewatrUpdater(ServiceRestClient serviceRestClient, RetryExecutor retryExecutor, IngredientsProperties ingredientsProperties, IngredientWarehouse ingredientWarehouse) {
        this.serviceRestClient = serviceRestClient;
        this.retryExecutor = retryExecutor;
        this.ingredientsProperties = ingredientsProperties;
        this.ingredientWarehouse = ingredientWarehouse;
    }

    void updateIfLimitReached(Ingredients ingredients) {
        if (ingredientsMatchTheThreshold(ingredients)) {
            notifyDojrzewatr();
            updateDatabaseStatus();
        }
    }

    private boolean ingredientsMatchTheThreshold(Ingredients ingredients) {
        boolean allIngredientsPresent = ingredients.ingredients.size() == IngredientType.values().length;
        boolean allIngredientsOverThreshold = ingredients.ingredients.stream().allMatch(ingredient -> ingredient.getQuantity() >= ingredientsProperties.getThreshold());
        return allIngredientsPresent && allIngredientsOverThreshold;
    }

    private void notifyDojrzewatr() {
        serviceRestClient.forService("dojrzewatr")
                .retryUsing(retryExecutor)
                .post()
                .withCircuitBreaker(withGroupKey(asKey("dojrzewatr_notification")), () -> {
                    log.error("Can't connect to dojrzewatr");
                    return "";
                })
                .onUrl("/brew")
                .body("")
                .andExecuteFor()
                .ignoringResponseAsync();
    }

    private void updateDatabaseStatus() {
        ingredientWarehouse.clearWarehouseByThreshold(ingredientsProperties.getThreshold());
    }
}
