package com.auction.infrastructure.repository;

import com.auction.application.port.ItemRepository;
import com.auction.domain.model.item.Item;
import com.auction.contract.vocab.ItemType;
import com.auction.infrastructure.persistence.DataSourceProvider;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcItemRepository implements ItemRepository {
    private final DataSourceProvider dataSourceProvider;

    public JdbcItemRepository(DataSourceProvider dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    @Override
    public Optional<Item> findById(String itemId) {
        String sql = "SELECT id, seller_id, name, description, starting_price " +
                "FROM items WHERE id = ?";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, itemId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding item by id: " + itemId, e);
        }
    }

    @Override
    public List<Item> findBySellerId(String sellerId) {
        String sql = "SELECT id, seller_id, name, description, starting_price " +
                "FROM items WHERE seller_id = ?";
        List<Item> items = new ArrayList<>();
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sellerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding items by sellerId: " + sellerId, e);
        }
    }

    @Override
    public void save(Item item) {
        String sql = "MERGE INTO items (id, seller_id, name, description, starting_price) " +
                "KEY(id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, item.getId());
            stmt.setString(2, item.getSellerId());
            stmt.setString(3, item.getName());
            stmt.setString(4, item.getDescription());
            stmt.setBigDecimal(5, item.getStartingPrice());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving item: " + item.getId(), e);
        }
    }

    @Override
    public void deleteById(String itemId) {
        String sql = "DELETE FROM items WHERE id = ?";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, itemId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting item by id: " + itemId, e);
        }
    }

    private Item mapRow(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String sellerId = rs.getString("seller_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        BigDecimal startingPrice = rs.getBigDecimal("starting_price");

        return new Item(id, sellerId, name, description, startingPrice) {
            @Override
            public ItemType getType() {
                return ItemType.ELECTRONICS;
            }

            @Override
            protected String describeDetails() {
                return getDescription();
            }
        };
    }
}