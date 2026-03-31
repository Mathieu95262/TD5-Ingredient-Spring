package edu.hei.school.ingredient.entity;

import lombok.*;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DishIngredient {

    @EqualsAndHashCode.Include
    private Dish dish;

    @EqualsAndHashCode.Include
    private Ingredient ingredient;

    private Double quantity;
    private Unit unit;
}