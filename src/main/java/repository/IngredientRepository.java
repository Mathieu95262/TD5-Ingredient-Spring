package edu.hei.school.ingredient.repository;

import edu.hei.school.ingredient.entity.CategoryEnum;
import edu.hei.school.ingredient.entity.Ingredient;
import edu.hei.school.ingredient.entity.StockValue;
import edu.hei.school.ingredient.entity.Unit;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class IngredientRepository {

    private final DataSource dataSource;

    public IngredientRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Ingredient> findAll() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT id, name, price, category FROM ingredient ORDER BY id")) {
            ResultSet rs = ps.executeQuery();
            List<Ingredient> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Ingredient> findById(Integer id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT id, name, price, category FROM ingredient WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean existsById(Integer id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT 1 FROM ingredient WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stock agrégé à la date {@code at} pour l'unité demandée (logique métier TD JDBC / mouvements).
     */
    public StockValue getStockValueAt(Instant at, Integer ingredientId, Unit unit) {
        String sql = """
                SELECT COALESCE(SUM(CASE
                    WHEN type = 'IN' THEN quantity
                    WHEN type = 'OUT' THEN -quantity
                    ELSE 0 END), 0) AS actual_quantity
                FROM stock_movement
                WHERE id_ingredient = ? AND unit = ?::unit AND creation_datetime <= ?
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, ingredientId);
            ps.setString(2, unit.name());
            ps.setTimestamp(3, Timestamp.from(at));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                double qty = rs.getDouble("actual_quantity");
                StockValue stockValue = new StockValue();
                stockValue.setQuantity(qty);
                stockValue.setUnit(unit);
                return stockValue;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Ingredient mapRow(ResultSet rs) throws SQLException {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(rs.getInt("id"));
        ingredient.setName(rs.getString("name"));
        ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));
        ingredient.setPrice(rs.getObject("price") == null ? null : rs.getDouble("price"));
        return ingredient;
    }
}
