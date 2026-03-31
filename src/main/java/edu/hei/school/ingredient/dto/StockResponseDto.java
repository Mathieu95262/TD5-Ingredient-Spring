package edu.hei.school.ingredient.dto;

import edu.hei.school.ingredient.entity.Unit;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockResponseDto {
    private Unit unit;
    private double value;
}