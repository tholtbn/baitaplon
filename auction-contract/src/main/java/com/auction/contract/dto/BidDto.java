package com.auction.application.dto;

public record BidDto(String id, String auctionId, String bidderId,
                     BigDecimal amount, Instant placedAt) {}
