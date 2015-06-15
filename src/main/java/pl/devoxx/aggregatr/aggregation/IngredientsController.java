package pl.devoxx.aggregatr.aggregation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pl.devoxx.aggregatr.aggregation.model.Ingredients;
import pl.devoxx.aggregatr.aggregation.model.Order;
import pl.devoxx.aggregatr.aggregation.model.Version;

@RestController
@RequestMapping(value = "/ingredients", consumes = Version.AGGREGATOR_V1, produces = MediaType.APPLICATION_JSON_VALUE)
public class IngredientsController {

    private final IngredientsAggregator ingredientsAggregator;

    @Autowired
    public IngredientsController(IngredientsAggregator ingredientsAggregator) {
        this.ingredientsAggregator = ingredientsAggregator;
    }

    @RequestMapping(method = RequestMethod.POST)
    public Ingredients distributeIngredients(@RequestBody Order order) {
        return ingredientsAggregator.fetchIngredients(order);
    }

}
