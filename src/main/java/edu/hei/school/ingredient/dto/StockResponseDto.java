package edu.hei.school.ingredient.dto;

import edu.hei.school.ingredient.entity.Unit;

public class StockResponseDto {
    private Unit unit;
    private double value;

    public StockResponseDto() {
    }

    public StockResponseDto(Unit unit, double value) {
        this.unit = unit;
        this.value = value;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
