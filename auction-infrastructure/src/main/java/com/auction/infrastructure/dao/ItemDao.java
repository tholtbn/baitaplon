package com.auction.infrastructure.dao;

import com.auction.infrastructure.persistence.DataSourceProvider;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ItemDao extends BaseDao {

    public record ItemRow(
            String id,
            String sellerId,
            String name,
            String description,
            BigDecimal startingPrice,
            String type,
            String status
    ) {}

    public ItemDao(DataSourceProvider dataSourceProvider) {
        super(dataSourceProvider);
    }

    public int insert(ItemRow row) {
        String sql = """
                INSERT INTO items (id, seller_id, name, description, starting_price, type, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        return executeUpdate(sql, stmt -> {
            stmt.setString(1, row.id());
            stmt.setString(2, row.sellerId());
            stmt.setString(3, row.name());
            stmt.setString(4, row.description());
            stmt.setBigDecimal(5, row.startingPrice());
            stmt.setString(6, row.type());
            stmt.setString(7, row.status());
        });
    }

    public int update(ItemRow row) {
        String sql = """
                UPDATE items
                SET name = ?, description = ?, starting_price = ?, type = ?, status = ?
                WHERE id = ?
                """;
        return executeUpdate(sql, stmt -> {
            stmt.setString(1, row.name());
            stmt.setString(2, row.description());
            stmt.setBigDecimal(3, row.startingPrice());
            stmt.setString(4, row.type());
            stmt.setString(5, row.status());
            stmt.setString(6, row.id());
        });
    }

    public Optional<ItemRow> findById(String id) {
        String sql = """
                SELECT id, seller_id, name, description, starting_price, type, status
                FROM items WHERE id = ?
                """;
        return queryOne(sql, stmt -> stmt.setString(1, id), this::mapRow);
    }

    public List<ItemRow> findAll() {
        String sql = """
                SELECT id, seller_id, name, description, starting_price, type, status
                FROM items
                """;
        return queryList(sql, null, this::mapRow);
    }

    public int deleteById(String id) {
        String sql = "DELETE FROM items WHERE id = ?";
        return executeUpdate(sql, stmt -> stmt.setString(1, id));
    }

    private ItemRow mapRow(ResultSet rs) throws SQLException {
        return new ItemRow(
                rs.getString("id"),
                rs.getString("seller_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBigDecimal("starting_price"),
                rs.getString("type"),
                rs.getString("status")
        );
    }
}