package com.auction.infrastructure.dao;

import com.auction.infrastructure.persistence.DataSourceProvider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UserDao extends BaseDao {

    public record UserRow(String id, String username, String passwordHash, String role) {}

    public UserDao(DataSourceProvider dataSourceProvider) {
        super(dataSourceProvider);
    }

    public int insert(UserRow row) {
        String sql = """
                INSERT INTO users (id, username, password_hash, role)
                VALUES (?, ?, ?, ?)
                """;
        return executeUpdate(sql, stmt -> {
            stmt.setString(1, row.id());
            stmt.setString(2, row.username());
            stmt.setString(3, row.passwordHash());
            stmt.setString(4, row.role());
        });
    }

    public Optional<UserRow> findById(String id) {
        String sql = "SELECT id, username, password_hash, role FROM users WHERE id = ?";
        return queryOne(sql, stmt -> stmt.setString(1, id), this::mapRow);
    }

    public Optional<UserRow> findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, role FROM users WHERE username = ?";
        return queryOne(sql, stmt -> stmt.setString(1, username), this::mapRow);
    }

    public List<UserRow> findAll() {
        String sql = "SELECT id, username, password_hash, role FROM users";
        return queryList(sql, null, this::mapRow);
    }

    private UserRow mapRow(ResultSet rs) throws SQLException {
        return new UserRow(
                rs.getString("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("role")
        );
    }
}