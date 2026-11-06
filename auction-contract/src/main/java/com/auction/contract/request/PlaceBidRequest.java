package com.auction.contract.request;

public record PlaceBidRequest(String auctionId, BigDecimal amount) {}
