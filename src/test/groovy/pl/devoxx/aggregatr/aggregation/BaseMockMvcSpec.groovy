package pl.devoxx.aggregatr.aggregation
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc
import pl.devoxx.aggregatr.aggregation.model.Ingredient
import pl.devoxx.aggregatr.aggregation.model.IngredientType
import pl.devoxx.aggregatr.aggregation.model.Ingredients
import spock.lang.Specification

abstract class BaseMockMvcSpec extends Specification {

    IngredientsAggregator ingredientsAggregator = Stub()

    def setup() {
        setupMocks()
        RestAssuredMockMvc.standaloneSetup(new IngredientsController(ingredientsAggregator))
    }

    void setupMocks() {
        ingredientsAggregator.fetchIngredients() >> new Ingredients([
                new Ingredient(IngredientType.MALT, 100),
                new Ingredient(IngredientType.WATER, 200),
                new Ingredient(IngredientType.HOP, 300),
                new Ingredient(IngredientType.YIEST, 400)
        ])
    }

}
