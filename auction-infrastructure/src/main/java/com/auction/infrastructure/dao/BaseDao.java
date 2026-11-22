package com.auction.infrastructure.dao;

import com.auction.infrastructure.persistence.DataSourceProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class BaseDao {
    protected final DataSourceProvider dataSourceProvider;

    protected BaseDao(DataSourceProvider dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    @FunctionalInterface
    protected interface StatementBinder {
        void bind(PreparedStatement stmt) throws SQLException;
    }

    @FunctionalInterface
    protected interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    protected Connection getConnection() throws SQLException {
        return dataSourceProvider.getConnection();
    }

    protected int executeUpdate(String sql, StatementBinder binder) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (binder != null) {
                binder.bind(stmt);
            }
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi thực thi SQL cập nhật: " + sql, e);
        }
    }

    protected <T> Optional<T> queryOne(String sql, StatementBinder binder, RowMapper<T> mapper) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (binder != null) {
                binder.bind(stmt);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(mapper.map(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi truy vấn một dòng SQL: " + sql, e);
        }
    }

    protected <T> List<T> queryList(String sql, StatementBinder binder, RowMapper<T> mapper) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (binder != null) {
                binder.bind(stmt);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                List<T> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapper.map(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Lỗi truy vấn danh sách SQL: " + sql, e);
        }
    }
}