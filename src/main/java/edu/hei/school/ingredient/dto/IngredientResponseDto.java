package edu.hei.school.ingredient.dto;

import edu.hei.school.ingredient.entity.CategoryEnum;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientResponseDto {
    private Integer id;
    private String name;
    private CategoryEnum category;
    private Double price;
}