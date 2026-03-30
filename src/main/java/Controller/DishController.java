package edu.hei.school.ingredient.controller;

import edu.hei.school.ingredient.dto.DishResponseDto;
import edu.hei.school.ingredient.dto.IngredientPayloadDto;
import edu.hei.school.ingredient.dto.IngredientResponseDto;
import edu.hei.school.ingredient.entity.Dish;
import edu.hei.school.ingredient.entity.DishIngredient;
import edu.hei.school.ingredient.entity.Ingredient;
import edu.hei.school.ingredient.repository.DishRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dishes")
public class DishController {

    private static final String BODY_REQUIRED_MSG =
            "Request body is required (JSON array of ingredients).";

    private final DishRepository dishRepository;

    public DishController(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    @GetMapping
    public List<DishResponseDto> listDishes() {
        return dishRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}/ingredients")
    public ResponseEntity<String> replaceIngredients(
            @PathVariable Integer id,
            @RequestBody(required = false) List<IngredientPayloadDto> body) {

        if (body == null) {
            return ResponseEntity.status(400)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(BODY_REQUIRED_MSG);
        }

        Optional<Dish> dishOpt = dishRepository.findById(id);
        if (dishOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Dish.id=" + id + ") is not found");
        }

        List<Integer> requestedIds = body.stream()
                .map(IngredientPayloadDto::getId)
                .filter(i -> i != null)
                .toList();
        List<Integer> toLink = dishRepository.filterExistingIngredientIds(requestedIds);
        dishRepository.replaceIngredientsForDish(id, toLink);
        return ResponseEntity.noContent().build();
    }

    private DishResponseDto toDto(Dish dish) {
        DishResponseDto dto = new DishResponseDto();
        dto.setId(dish.getId());
        dto.setName(dish.getName());
        dto.setSellingPrice(dish.getPrice());
        if (dish.getDishIngredients() != null) {
            dto.setIngredients(dish.getDishIngredients().stream()
                    .map(DishIngredient::getIngredient)
                    .map(this::ingredientToDto)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private IngredientResponseDto ingredientToDto(Ingredient i) {
        return new IngredientResponseDto(i.getId(), i.getName(), i.getCategory(), i.getPrice());
    }
}
