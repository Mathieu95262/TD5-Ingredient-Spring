package edu.hei.school.ingredient.entity;

import lombok.*;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
public class Order {

    private Integer id;
    private String reference;
    private Instant creationDatetime;
    private List<DishOrder> dishOrderList;

    public Double getTotalAmountWithoutVat() {
        if (dishOrderList == null) {
            return null;
        }
        double amount = 0.0;
        for (DishOrder dishOrder : dishOrderList) {
            amount = amount + dishOrder.getQuantity() * dishOrder.getDish().getPrice();
        }
        return amount;
    }

    public Double getTotalAmountWithVat() {
        return getTotalAmountWithoutVat() == null ? null : getTotalAmountWithoutVat() * 1.2;
    }
}