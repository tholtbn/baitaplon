package com.auction.application.event;

import java.time.Instant;

public interface DomainEvent {
    String auctionId();
    Instant occurredAt();
}
