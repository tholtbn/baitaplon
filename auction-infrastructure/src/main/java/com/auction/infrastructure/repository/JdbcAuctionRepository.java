package com.auction.infrastructure.repository;

import com.auction.application.port.AuctionRepository;
import com.auction.contract.vocab.AuctionStatus;
import com.auction.domain.model.auction.Auction;
import com.auction.domain.model.auction.BidTransaction;
import com.auction.domain.rule.AntiSnipingPolicy;
import com.auction.infrastructure.persistence.DataSourceProvider;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcAuctionRepository implements AuctionRepository {
    private final DataSourceProvider dataSourceProvider;

    public JdbcAuctionRepository(DataSourceProvider dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    @Override
    public Optional<Auction> findById(String id) {
        String sql = "SELECT id, item_id, start_time, end_time, starting_price, current_price, leader_id, status, version " +
                "FROM auctions WHERE id = ?";
        try (Connection conn = dataSourceProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    List<BidTransaction> bids = findBidsByAuctionId(conn, id);
                    Timestamp startTs = rs.getTimestamp("start_time");
                    Timestamp endTs = rs.getTimestamp("end_time");

                    Auction auction = Auction.restore(
                            rs.getString("id"),
                            rs.getString("item_id"),
                            "unknown-seller",
                            rs.getBigDecimal("starting_price"),
                            rs.getBigDecimal("current_price"),
                            rs.getString("leader_id"),
                            startTs != null ? startTs.toInstant() : Instant.now(),
                            endTs != null ? endTs.toInstant() : Instant.now().plusSeconds(3600),
                            AntiSnipingPolicy.disabled(),
                            AuctionStatus.valueOf(rs.getString("status")),
                            bids
                    );
                    return Optional.of(auction);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding auction by id: " + id, e);
        }
    }

    @Override
    public List<Auction> findAll() {
        return List.of();
    }

    @Override
    public List<Auction> findByStatus(AuctionStatus status) {
        return List.of();
    }

    @Override
    public List<Auction> findBySellerId(String sellerId) {
        return List.of();
    }

    private List<BidTransaction> findBidsByAuctionId(Connection conn, String auctionId) throws SQLException {
        List<BidTransaction> bids = new ArrayList<>();
        String sql = "SELECT id, auction_id, bidder_id, amount, timestamp " +
                "FROM bids WHERE auction_id = ? ORDER BY timestamp ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, auctionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp bidTs = rs.getTimestamp("timestamp");
                    bids.add(new BidTransaction(
                            rs.getString("id"),
                            rs.getString("auction_id"),
                            rs.getString("bidder_id"),
                            rs.getBigDecimal("amount"),
                            bidTs != null ? bidTs.toInstant() : Instant.now()
                    ));
                }
            }
        }
        return bids;
    }

    @Override
    public void save(Auction auction) {
        String upsertAuction = "MERGE INTO auctions " +
                "(id, item_id, start_time, end_time, starting_price, current_price, leader_id, status, version) " +
                "KEY(id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String upsertBid = "MERGE INTO bids " +
                "(id, auction_id, bidder_id, amount, timestamp) " +
                "KEY(id) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = dataSourceProvider.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmtA = conn.prepareStatement(upsertAuction)) {
                BigDecimal startingPrice = auction.getStartingPrice();
                BigDecimal currentPrice = auction.getCurrentPrice() != null ? auction.getCurrentPrice() : startingPrice;
                String leaderId = auction.getLeaderId().orElse(null);

                stmtA.setString(1, auction.getId());
                stmtA.setString(2, auction.getItemId());
                stmtA.setTimestamp(3, Timestamp.from(auction.getStartTime()));
                stmtA.setTimestamp(4, Timestamp.from(auction.getEndTime()));
                stmtA.setBigDecimal(5, startingPrice);
                stmtA.setBigDecimal(6, currentPrice);
                stmtA.setString(7, leaderId);
                stmtA.setString(8, auction.getStatus().name());
                stmtA.setInt(9, 0);
                stmtA.executeUpdate();
            }

            try (PreparedStatement stmtB = conn.prepareStatement(upsertBid)) {
                for (BidTransaction bid : auction.getBids()) {
                    stmtB.setString(1, bid.getId());
                    stmtB.setString(2, bid.getAuctionId());
                    stmtB.setString(3, bid.getBidderId());
                    stmtB.setBigDecimal(4, bid.getAmount());
                    stmtB.setTimestamp(5, Timestamp.from(bid.getPlacedAt()));
                    stmtB.addBatch();
                }
                stmtB.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Error saving auction transaction", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void deleteById(String auctionId) {

    }
}
