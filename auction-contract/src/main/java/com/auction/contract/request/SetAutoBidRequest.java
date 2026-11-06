package com.auction.contract.request;

public record SetAutoBidRequest(String auctionId, BigDecimal maxBid, BigDecimal increment) {}
