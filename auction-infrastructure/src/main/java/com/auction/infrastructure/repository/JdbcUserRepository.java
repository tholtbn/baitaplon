package com.auction.infrastructure.repository;

import com.auction.application.port.UserRepository;
import com.auction.contract.vocab.Role;
import com.auction.domain.model.user.Admin;
import com.auction.domain.model.user.Bidder;
import com.auction.domain.model.user.Seller;
import com.auction.domain.model.user.User;
import com.auction.infrastructure.persistence.DataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {
    private final DataSourceProvider dataSourceProvider;

    public JdbcUserRepository(DataSourceProvider dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    @Override
    public Optional<User> findById(String userId) {
        String sql = "SELECT id, username, password_hash, role FROM users WHERE id = ?";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find user by id: " + userId, e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, role FROM users WHERE username = ?";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to find user by username: " + username, e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT id, username, password_hash, role FROM users";
        List<User> users = new ArrayList<>();
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query all users", e);
        }
    }

    @Override
    public void save(User user) {
        String sql = "MERGE INTO users (id, username, password_hash, role) KEY(id) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save user: " + user.getId(), e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        String roleStr = rs.getString("role");
        String placeholderEmail = (username != null && !username.isBlank())
                ? username + "@local.invalid"
                : "user@local.invalid";

        Role role = Role.valueOf(roleStr);
        return switch (role) {
            case ADMIN -> new Admin(id, username, passwordHash, placeholderEmail);
            case BIDDER -> new Bidder(id, username, passwordHash, placeholderEmail);
            case SELLER -> new Seller(id, username, passwordHash, placeholderEmail);
        };
    }
}