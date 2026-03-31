package edu.hei.school.ingredient.entity;

import lombok.*;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DishOrder {

    @EqualsAndHashCode.Include
    private Integer id;

    private Dish dish;
    private Integer quantity;
}