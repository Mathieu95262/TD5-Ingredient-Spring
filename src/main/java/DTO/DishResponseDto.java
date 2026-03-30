package edu.hei.school.ingredient.dto;

import java.util.List;

public class DishResponseDto {
    private Integer id;
    private String name;
    private Double sellingPrice;
    private List<IngredientResponseDto> ingredients;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(Double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public List<IngredientResponseDto> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<IngredientResponseDto> ingredients) {
        this.ingredients = ingredients;
    }
}
