package edu.hei.school.ingredient.entity;

import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
public class Dish {

    private Integer id;
    private Double price;
    private String name;
    private DishTypeEnum dishType;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DishIngredient> dishIngredients;

    public void setDishIngredients(List<DishIngredient> dishIngredients) {
        if (dishIngredients == null) {
            this.dishIngredients = new ArrayList<>();
            return;
        }
        for (DishIngredient ingredient : dishIngredients) {
            ingredient.setDish(this);
        }
        this.dishIngredients = dishIngredients;
    }

    public Double getDishCost() {
        double totalPrice = 0;
        for (DishIngredient dishIngredient : dishIngredients) {
            Double quantity = dishIngredient.getQuantity();
            if (quantity == null) {
                throw new RuntimeException("Some ingredients have undefined quantity");
            }
            totalPrice = totalPrice + dishIngredient.getIngredient().getPrice() * quantity;
        }
        return totalPrice;
    }

    public Double getGrossMargin() {
        if (price == null) {
            throw new RuntimeException("Price is null");
        }
        return price - getDishCost();
    }
}