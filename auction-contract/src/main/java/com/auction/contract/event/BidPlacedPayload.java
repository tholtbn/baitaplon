package com.auction.contract.event;

import com.auction.contract.dto.BidDto;
import java.math.BigDecimal;
import java.time.Instant;

public record BidPlacedPayload(String auctionId, BidDto bid, BigDecimal currentPrice,
                               String leaderId, Instant endTime) {}