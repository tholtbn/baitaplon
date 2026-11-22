package com.auction.infrastructure.dao;

import com.auction.infrastructure.persistence.DataSourceProvider;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class AuctionDao extends BaseDao {

    public record AuctionRow(
            String id,
            String itemId,
            Instant startTime,
            Instant endTime,
            BigDecimal startingPrice,
            BigDecimal currentPrice,
            String leaderId,
            String status,
            int version
    ) {}

    public AuctionDao(DataSourceProvider dataSourceProvider) {
        super(dataSourceProvider);
    }

    public int insert(AuctionRow row) {
        String sql = """
                INSERT INTO auctions (
                    id, item_id, start_time, end_time, starting_price,
                    current_price, leader_id, status, version
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        return executeUpdate(sql, stmt -> {
            stmt.setString(1, row.id());
            stmt.setString(2, row.itemId());
            stmt.setTimestamp(3, Timestamp.from(row.startTime()));
            stmt.setTimestamp(4, Timestamp.from(row.endTime()));
            stmt.setBigDecimal(5, row.startingPrice());
            stmt.setBigDecimal(6, row.currentPrice());
            stmt.setString(7, row.leaderId());
            stmt.setString(8, row.status());
            stmt.setInt(9, row.version());
        });
    }

    public int updateState(AuctionRow row) {
        String sql = """
                UPDATE auctions
                SET end_time = ?, current_price = ?, leader_id = ?, status = ?, version = version + 1
                WHERE id = ? AND version = ?
                """;
        return executeUpdate(sql, stmt -> {
            stmt.setTimestamp(1, Timestamp.from(row.endTime()));
            stmt.setBigDecimal(2, row.currentPrice());
            stmt.setString(3, row.leaderId());
            stmt.setString(4, row.status());
            stmt.setString(5, row.id());
            stmt.setInt(6, row.version());
        });
    }

    public Optional<AuctionRow> findById(String id) {
        String sql = """
                SELECT id, item_id, start_time, end_time, starting_price,
                       current_price, leader_id, status, version
                FROM auctions WHERE id = ?
                """;
        return queryOne(sql, stmt -> stmt.setString(1, id), this::mapRow);
    }

    public List<AuctionRow> findAll() {
        String sql = """
                SELECT id, item_id, start_time, end_time, starting_price,
                       current_price, leader_id, status, version
                FROM auctions
                """;
        return queryList(sql, null, this::mapRow);
    }

    private AuctionRow mapRow(ResultSet rs) throws SQLException {
        Timestamp startTs = rs.getTimestamp("start_time");
        Timestamp endTs = rs.getTimestamp("end_time");
        return new AuctionRow(
                rs.getString("id"),
                rs.getString("item_id"),
                startTs != null ? startTs.toInstant() : null,
                endTs != null ? endTs.toInstant() : null,
                rs.getBigDecimal("starting_price"),
                rs.getBigDecimal("current_price"),
                rs.getString("leader_id"),
                rs.getString("status"),
                rs.getInt("version")
        );
    }
}