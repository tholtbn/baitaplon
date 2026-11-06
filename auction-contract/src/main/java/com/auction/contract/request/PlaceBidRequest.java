package com.auction.contract.request;
import java.math.BigDecimal;

public record PlaceBidRequest(String auctionId, BigDecimal amount) {}
