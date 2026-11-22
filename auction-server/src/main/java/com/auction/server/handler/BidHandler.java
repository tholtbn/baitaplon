package com.auction.server.handler;

import com.auction.server.network.ClientSession;
import com.google.gson.JsonObject;

import java.math.BigDecimal;

public class BidHandler {

    public JsonObject handlePlaceBid(JsonObject req, ClientSession session) {
        String bidderId = session.getUserId();
        String auctionId = req.has("auctionId") ? req.get("auctionId").getAsString() : null;
        BigDecimal amount = req.has("amount") ? req.get("amount").getAsBigDecimal() : null;

        if (auctionId == null || auctionId.isBlank() || amount == null) {
            throw new IllegalArgumentException("auctionId and amount are required");
        }

        // TODO: Gọi biddingService.placeBid(auctionId, bidderId, amount)
        // BiddingService sẽ dùng Clock của server, khóa theo auctionId, lưu repository và publish event
        JsonObject data = new JsonObject();
        data.addProperty("auctionId", auctionId);
        data.addProperty("bidderId", bidderId);
        data.addProperty("amount", amount);
        return data;
    }
}