package pl.devoxx.aggregatr.aggregation
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc
import pl.devoxx.aggregatr.aggregation.model.Ingredient
import pl.devoxx.aggregatr.aggregation.model.Ingredients
import pl.devoxx.aggregatr.aggregation.model.Order
import spock.lang.Specification

abstract class BaseMockMvcSpec extends Specification {

    protected static final int QUANTITY = 200

//    IngredientsAggregator ingredientsAggregator = Stub()

    def setup() {
        setupMocks()
//        RestAssuredMockMvc.standaloneSetup(new IngredientsController(ingredientsAggregator))
    }

    void setupMocks() {
        ingredientsAggregator.fetchIngredients(_) >> { Order order ->
            return new Ingredients(order.items.collect { new Ingredient(it, QUANTITY)})
        }
    }

}
