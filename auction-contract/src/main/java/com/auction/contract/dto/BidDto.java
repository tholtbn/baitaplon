// dto/BidDto.java
package com.auction.contract.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record BidDto(String id, String auctionId, String bidderId,
                     BigDecimal amount, Instant placedAt) {}