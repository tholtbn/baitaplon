package com.auction.application.event;

import java.time.Instant;
import java.math.BigDecimal;

public record AuctionFinishedEvent(String auctionId, String winnerId, BigDecimal finalPrice,
                                   Instant occurredAt) implements DomainEvent {
    public AuctionFinishedEvent {
                if (auctionId == null || finalPrice == null || occurredAt == null) {
                        throw new IllegalArgumentException("auctionId, finalPrice and occurredAt are required");
                    }
            }

            public boolean hasWinner() {
                return winnerId != null;
            }
}
