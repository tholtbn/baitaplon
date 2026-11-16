package com.auction.application.event;

import com.auction.domain.model.auction.BidTransaction;
import java.math.BigDecimal;
import java.time.Instant;

public record BidPlacedEvent(String auctionId, BidTransaction bid, BigDecimal currentPrice,
                             String leaderId, Instant endTime, Instant occurredAt) implements DomainEvent {}
