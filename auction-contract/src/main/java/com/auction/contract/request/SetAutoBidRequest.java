package com.auction.contract.request;
import java.math.BigDecimal;

public record SetAutoBidRequest(String auctionId, BigDecimal maxBid, BigDecimal increment) {}

