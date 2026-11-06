package com.auction.domain.model.auction;

import com.auction.domain.model.Entity;
import com.auction.domain.model.Guard;
import java.math.BigDecimal;
import java.time.Instant;

/** Một lượt đặt giá hợp lệ. Bất biến sau khi tạo. */
public class BidTransaction extends Entity {

    private final String auctionId;
    private final String bidderId;
    private final BigDecimal amount;
    private final Instant placedAt;

    public BidTransaction(
            String id, String auctionId, String bidderId, BigDecimal amount, Instant placedAt) {
        super(id);
        this.auctionId = Guard.requireNotBlank(auctionId, "auctionId");
        this.bidderId = Guard.requireNotBlank(bidderId, "bidderId");
        this.amount = Guard.requirePositive(amount, "amount");
        this.placedAt = Guard.requireNonNull(placedAt, "placedAt");
    }

    public String getAuctionId() {
        return auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getPlacedAt() {
        return placedAt;
    }

    @Override
    public String printInfo() {
        return "Bid " + amount.toPlainString() + " by " + bidderId + " at " + placedAt;
    }
}