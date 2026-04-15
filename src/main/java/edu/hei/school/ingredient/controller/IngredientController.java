package edu.hei.school.ingredient.controller;

import edu.hei.school.ingredient.dto.IngredientResponseDto;
import edu.hei.school.ingredient.dto.StockResponseDto;
import edu.hei.school.ingredient.entity.Ingredient;
import edu.hei.school.ingredient.entity.StockValue;
import edu.hei.school.ingredient.entity.Unit;
import edu.hei.school.ingredient.repository.IngredientRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/ingredients")
public class IngredientController {

    private static final String MSG_MISSING_QUERY =
            "Either mandatory query parameter `at` or `unit` is not provided.";

    private final IngredientRepository ingredientRepository;

    public IngredientController(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    @GetMapping
    public List<IngredientResponseDto> listIngredients() {
        return ingredientRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        Optional<Ingredient> opt = ingredientRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(404)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Ingredient.id=" + id + " is not found");
        }
        return ResponseEntity.ok(toDto(opt.get()));
    }

    @GetMapping("/{id}/stock")
    public ResponseEntity<?> getStock(
            @PathVariable Integer id,
            @RequestParam(required = false) String at,
            @RequestParam(required = false) String unit) {

        if (at == null || at.isBlank() || unit == null || unit.isBlank()) {
            return ResponseEntity.status(400)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(MSG_MISSING_QUERY);
        }

        if (!ingredientRepository.existsById(id)) {
            return ResponseEntity.status(404)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Ingredient.id=" + id + " is not found");
        }

        final Instant instant;
        try {
            instant = Instant.parse(at.trim());
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Invalid `at` instant (ISO-8601 expected).");
        }

        final Unit unitEnum;
        try {
            unitEnum = Unit.valueOf(unit.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Invalid `unit` (PCS, KG, L).");
        }

        StockValue stock = ingredientRepository.getStockValueAt(instant, id, unitEnum);
        return ResponseEntity.ok(new StockResponseDto(stock.getUnit(), stock.getQuantity()));
    }

    private IngredientResponseDto toDto(Ingredient i) {
        return new IngredientResponseDto(i.getId(), i.getName(), i.getCategory(), i.getPrice());
    }
}
