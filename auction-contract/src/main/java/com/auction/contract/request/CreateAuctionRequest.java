package com.auction.contract.request;
public record CreateAuctionRequest(String itemId, Instant startTime, Instant endTime,
                                   Long snipingWindowSeconds, Long snipingExtensionSeconds) {}    // null = không anti-sniping

