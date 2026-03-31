package edu.hei.school.ingredient.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
public class DishResponseDto {
    private Integer id;
    private String name;
    private Double sellingPrice;
    private List<IngredientResponseDto> ingredients;
}