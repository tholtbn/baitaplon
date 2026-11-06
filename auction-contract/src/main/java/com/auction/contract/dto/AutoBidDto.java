package com.auction.application.dto;


public record AutoBidDto(String auctionId, String bidderId, BigDecimal maxBid,
                         BigDecimal increment, Instant registeredAt) {}