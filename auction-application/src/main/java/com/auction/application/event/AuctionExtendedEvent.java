package com.auction.application.event;

import java.time.Instant;

public record AuctionExtendedEvent(String auctionId, Instant previousEndTime,
                                   Instant newEndTime, Instant occurredAt) implements DomainEvent {}
