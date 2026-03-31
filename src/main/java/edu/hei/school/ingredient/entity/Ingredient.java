package edu.hei.school.ingredient.entity;

import lombok.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import static java.time.Instant.now;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Ingredient {

    @EqualsAndHashCode.Include
    private Integer id;

    private String name;
    private CategoryEnum category;
    private Double price;
    private List<StockMovement> stockMovementList;

    public StockValue getStockValueAt(Instant t) {
        if (stockMovementList == null) {
            return null;
        }
        Map<Unit, List<StockMovement>> unitSet = stockMovementList.stream()
                .collect(Collectors.groupingBy(stockMovement -> stockMovement.getValue().getUnit()));

        if (unitSet.keySet().size() > 1) {
            throw new RuntimeException("Multiple unit found and not handle for conversion");
        }

        List<StockMovement> stockMovements = stockMovementList.stream()
                .filter(stockMovement -> !stockMovement.getCreationDatetime().isAfter(t))
                .toList();

        double movementIn = stockMovements.stream()
                .filter(stockMovement -> stockMovement.getType().equals(MovementTypeEnum.IN))
                .flatMapToDouble(stockMovement -> DoubleStream.of(stockMovement.getValue().getQuantity()))
                .sum();

        double movementOut = stockMovements.stream()
                .filter(stockMovement -> stockMovement.getType().equals(MovementTypeEnum.OUT))
                .flatMapToDouble(stockMovement -> DoubleStream.of(stockMovement.getValue().getQuantity()))
                .sum();

        StockValue stockValue = new StockValue();
        stockValue.setQuantity(movementIn - movementOut);
        stockValue.setUnit(unitSet.keySet().stream().findFirst().get());

        return stockValue;
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", price=" + price +
                ", actualStock=" + getStockValueAt(now()) +
                '}';
    }
}