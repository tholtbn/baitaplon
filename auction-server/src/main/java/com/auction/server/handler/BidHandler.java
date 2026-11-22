package com.auction.server.handler;

import com.auction.server.network.ClientSession;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Objects;

public class BidHandler {
    private final Object biddingService; // Thay bằng BiddingService thật khi tầng application ổn định
    private final Clock clock;

    public BidHandler(Object biddingService, Clock clock) {
        this.biddingService = Objects.requireNonNull(biddingService, "biddingService must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    public JsonObject handlePlaceBid(JsonObject req, ClientSession session) {
        String bidderId = session.getUserId();
        if (bidderId == null || bidderId.isBlank()) {
            throw new IllegalStateException("Session is not authenticated");
        }
        if (req == null || !req.has("auctionId") || !req.has("amount")) {
            throw new IllegalArgumentException("auctionId and amount are required");
        }

        String auctionId = req.get("auctionId").getAsString().trim();
        BigDecimal amount = req.get("amount").getAsBigDecimal();
        if (auctionId.isEmpty() || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid auctionId or amount");
        }

        // TODO: Gọi BiddingService đặt giá (dùng clock của server), ví dụ:
        // var bidResult = ((BiddingService) biddingService).placeBid(auctionId, bidderId, amount, clock.instant());

        JsonObject data = new JsonObject();
        data.addProperty("auctionId", auctionId);
        data.addProperty("bidderId", bidderId);
        data.addProperty("amount", amount);
        data.addProperty("timestamp", clock.instant().toString());
        return data;
    }
}