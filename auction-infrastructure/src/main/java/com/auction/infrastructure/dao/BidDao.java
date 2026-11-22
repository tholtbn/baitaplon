package com.auction.infrastructure.dao;

import com.auction.infrastructure.persistence.DataSourceProvider;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class BidDao extends BaseDao {

    public record BidRow(
            String id,
            String auctionId,
            String bidderId,
            BigDecimal amount,
            Instant timestamp
    ) {}

    public BidDao(DataSourceProvider dataSourceProvider) {
        super(dataSourceProvider);
    }

    public int insert(BidRow row) {
        String sql = """
                INSERT INTO bids (id, auction_id, bidder_id, amount, timestamp)
                VALUES (?, ?, ?, ?, ?)
                """;
        return executeUpdate(sql, stmt -> {
            stmt.setString(1, row.id());
            stmt.setString(2, row.auctionId());
            stmt.setString(3, row.bidderId());
            stmt.setBigDecimal(4, row.amount());
            stmt.setTimestamp(5, Timestamp.from(row.timestamp()));
        });
    }

    public List<BidRow> findByAuctionId(String auctionId) {
        String sql = """
                SELECT id, auction_id, bidder_id, amount, timestamp
                FROM bids WHERE auction_id = ? ORDER BY timestamp ASC
                """;
        return queryList(sql, stmt -> stmt.setString(1, auctionId), this::mapRow);
    }

    private BidRow mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("timestamp");
        return new BidRow(
                rs.getString("id"),
                rs.getString("auction_id"),
                rs.getString("bidder_id"),
                rs.getBigDecimal("amount"),
                ts != null ? ts.toInstant() : null
        );
    }
}