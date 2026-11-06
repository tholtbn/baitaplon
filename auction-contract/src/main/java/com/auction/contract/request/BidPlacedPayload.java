package com.auction.contract.request;

public record BidPlacedPayload(String auctionId, BidDto bid, BigDecimal currentPrice,
                               String leaderId, Instant endTime) {}