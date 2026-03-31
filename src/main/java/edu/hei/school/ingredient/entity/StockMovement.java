package edu.hei.school.ingredient.entity;

import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {

    private Integer id;
    private MovementTypeEnum type;
    private Instant creationDatetime;
    private StockValue value;
}