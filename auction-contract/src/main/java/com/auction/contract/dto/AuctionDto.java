package com.auction.application.dto;

import com.auction.contract.vocab.AuctionStatus;

public record AuctionDto(String id, ItemDto item, String sellerId, AuctionStatus status,
                         BigDecimal currentPrice, String leaderId, Instant startTime, Instant endTime,
                         int bidCount) {}