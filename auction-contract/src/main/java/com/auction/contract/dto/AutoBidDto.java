// dto/AutoBidDto.java
package com.auction.contract.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AutoBidDto(String auctionId, String bidderId, BigDecimal maxBid,
                         BigDecimal increment, Instant registeredAt) {}