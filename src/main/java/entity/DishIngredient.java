package edu.hei.school.ingredient.entity;

import java.util.Objects;

public class DishIngredient {
    private Dish dish;
    private Ingredient ingredient;
    private Double quantity;
    private Unit unit;

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    @Override
    public String toString() {
        return "DishIngredient{"
                + "ingredient=" + ingredient
                + ", quantity=" + quantity
                + ", unit=" + unit
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DishIngredient that)) {
            return false;
        }
        return Objects.equals(dish, that.dish) && Objects.equals(ingredient, that.ingredient)
                && Objects.equals(quantity, that.quantity) && unit == that.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dish, ingredient, quantity, unit);
    }
}
