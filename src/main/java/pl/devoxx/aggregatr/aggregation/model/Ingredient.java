package pl.devoxx.aggregatr.aggregation.model;

public class Ingredient {
    public IngredientType type;
    public Integer quantity;

    public Ingredient(IngredientType type, Integer quantity) {
        this.type = type;
        this.quantity = quantity;
    }

    public Ingredient() {
    }
}
