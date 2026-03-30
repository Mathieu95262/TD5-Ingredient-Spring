package edu.hei.school.ingredient.repository;

import edu.hei.school.ingredient.entity.CategoryEnum;
import edu.hei.school.ingredient.entity.Dish;
import edu.hei.school.ingredient.entity.DishIngredient;
import edu.hei.school.ingredient.entity.DishTypeEnum;
import edu.hei.school.ingredient.entity.Ingredient;
import edu.hei.school.ingredient.entity.Unit;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DishRepository {

    private static final double DEFAULT_LINK_QUANTITY = 1.0;
    private static final Unit DEFAULT_LINK_UNIT = Unit.PCS;

    private final DataSource dataSource;
    private final IngredientRepository ingredientRepository;

    public DishRepository(DataSource dataSource, IngredientRepository ingredientRepository) {
        this.dataSource = dataSource;
        this.ingredientRepository = ingredientRepository;
    }

    public List<Dish> findAll() {
        String sql = """
                SELECT d.id AS dish_id, d.name AS dish_name, d.selling_price, d.dish_type,
                       i.id AS ing_id, i.name AS ing_name, i.price AS ing_price, i.category AS ing_cat
                FROM dish d
                LEFT JOIN dish_ingredient di ON di.id_dish = d.id
                LEFT JOIN ingredient i ON i.id = di.id_ingredient
                ORDER BY d.id, di.id
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            Map<Integer, Dish> byId = new LinkedHashMap<>();
            while (rs.next()) {
                int dishId = rs.getInt("dish_id");
                Dish dish = byId.computeIfAbsent(dishId, id -> mapDishHeader(rs, id));
                if (rs.getObject("ing_id") != null) {
                    Ingredient ing = new Ingredient();
                    ing.setId(rs.getInt("ing_id"));
                    ing.setName(rs.getString("ing_name"));
                    ing.setPrice(rs.getObject("ing_price") == null ? null : rs.getDouble("ing_price"));
                    ing.setCategory(CategoryEnum.valueOf(rs.getString("ing_cat")));

                    DishIngredient di = new DishIngredient();
                    di.setDish(dish);
                    di.setIngredient(ing);
                    if (dish.getDishIngredients() == null) {
                        dish.setDishIngredients(new ArrayList<>());
                    }
                    dish.getDishIngredients().add(di);
                }
            }
            return new ArrayList<>(byId.values());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Dish mapDishHeader(ResultSet rs, int dishId) {
        try {
            Dish dish = new Dish();
            dish.setId(dishId);
            dish.setName(rs.getString("dish_name"));
            dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
            dish.setPrice(rs.getObject("selling_price") == null ? null : rs.getDouble("selling_price"));
            dish.setDishIngredients(new ArrayList<>());
            return dish;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Dish> findById(Integer id) {
        String sql = """
                SELECT d.id AS dish_id, d.name AS dish_name, d.selling_price, d.dish_type,
                       i.id AS ing_id, i.name AS ing_name, i.price AS ing_price, i.category AS ing_cat
                FROM dish d
                LEFT JOIN dish_ingredient di ON di.id_dish = d.id
                LEFT JOIN ingredient i ON i.id = di.id_ingredient
                WHERE d.id = ?
                ORDER BY di.id
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                Dish dish = mapDishHeader(rs, rs.getInt("dish_id"));
                do {
                    if (rs.getObject("ing_id") != null) {
                        Ingredient ing = new Ingredient();
                        ing.setId(rs.getInt("ing_id"));
                        ing.setName(rs.getString("ing_name"));
                        ing.setPrice(rs.getObject("ing_price") == null ? null : rs.getDouble("ing_price"));
                        ing.setCategory(CategoryEnum.valueOf(rs.getString("ing_cat")));

                        DishIngredient di = new DishIngredient();
                        di.setDish(dish);
                        di.setIngredient(ing);
                        dish.getDishIngredients().add(di);
                    }
                } while (rs.next());
                return Optional.of(dish);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void replaceIngredientsForDish(Integer dishId, List<Integer> ingredientIdsExistingInDb) {
        String deleteSql = "DELETE FROM dish_ingredient WHERE id_dish = ?";
        String insertSql = """
                INSERT INTO dish_ingredient (id, id_ingredient, id_dish, required_quantity, unit)
                VALUES (?, ?, ?, ?, ?::unit)
                """;
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement del = conn.prepareStatement(deleteSql)) {
                    del.setInt(1, dishId);
                    del.executeUpdate();
                }
                try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                    for (Integer ingredientId : ingredientIdsExistingInDb) {
                        ins.setInt(1, getNextSerialValue(conn, "dish_ingredient", "id"));
                        ins.setInt(2, ingredientId);
                        ins.setInt(3, dishId);
                        ins.setDouble(4, DEFAULT_LINK_QUANTITY);
                        ins.setString(5, DEFAULT_LINK_UNIT.name());
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Integer> filterExistingIngredientIds(List<Integer> requestedIds) {
        List<Integer> existing = new ArrayList<>();
        if (requestedIds == null) {
            return existing;
        }
        for (Integer id : requestedIds) {
            if (id != null && ingredientRepository.existsById(id)) {
                existing.add(id);
            }
        }
        return existing;
    }

    private String getSerialSequenceName(Connection conn, String tableName, String columnName)
            throws SQLException {
        String sql = "SELECT pg_get_serial_sequence(?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    private int getNextSerialValue(Connection conn, String tableName, String columnName)
            throws SQLException {
        String sequenceName = getSerialSequenceName(conn, tableName, columnName);
        if (sequenceName == null) {
            throw new IllegalArgumentException(
                    "Any sequence found for " + tableName + "." + columnName
            );
        }
        updateSequenceNextValue(conn, tableName, columnName, sequenceName);
        String nextValSql = "SELECT nextval(?)";
        try (PreparedStatement ps = conn.prepareStatement(nextValSql)) {
            ps.setString(1, sequenceName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private void updateSequenceNextValue(Connection conn, String tableName, String columnName,
                                         String sequenceName) throws SQLException {
        String setValSql = String.format(
                "SELECT setval('%s', (SELECT COALESCE(MAX(%s), 0) FROM %s))",
                sequenceName, columnName, tableName
        );
        try (PreparedStatement ps = conn.prepareStatement(setValSql)) {
            ps.executeQuery();
        }
    }
}
