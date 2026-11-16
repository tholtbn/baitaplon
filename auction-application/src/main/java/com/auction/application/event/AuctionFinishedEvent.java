package com.auction.application.event;

import java.time.Instant;

public record AuctionFinishedEvent(String auctionId, Instant occurredAt) implements DomainEvent {
    public AuctionFinishedEvent { if (auctionId == null || occurredAt == null) throw new IllegalArgumentException("event fields are required"); }
}
