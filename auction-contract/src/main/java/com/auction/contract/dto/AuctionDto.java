package com.auction.contract.dto;

import com.auction.contract.vocab.AuctionStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record AuctionDto(String id, ItemDto item, String sellerId, AuctionStatus status,
                         BigDecimal currentPrice, String leaderId, Instant startTime,
                         Instant endTime, int bidCount) {}