package com.auction.contract.request;
import java.time.Instant;

public record CreateAuctionRequest(String itemId, Instant startTime, Instant endTime,
                                   Long snipingWindowSeconds, Long snipingExtensionSeconds) {}    // null = không anti-sniping

