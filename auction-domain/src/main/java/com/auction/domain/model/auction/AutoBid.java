package com.auction.domain.model.auction;

import com.auction.domain.exception.ValidationException;
import com.auction.domain.model.Guard;
import java.math.BigDecimal;
import java.time.Instant;

/** Lệnh đấu giá tự động của một bidder trên một phiên. Bất biến. */
public record AutoBid(
        String bidderId, BigDecimal maxBid, BigDecimal increment, Instant registeredAt) {

    public AutoBid {
        Guard.requireNotBlank(bidderId, "bidderId");
        Guard.requirePositive(maxBid, "maxBid");
        Guard.requirePositive(increment, "increment");
        Guard.requireNonNull(registeredAt, "registeredAt");
        if (increment.compareTo(maxBid) > 0) {
            throw new ValidationException("increment must not exceed maxBid");
        }
    }

    boolean canAfford(BigDecimal amount) {
        return amount.compareTo(maxBid) <= 0;
    }
}