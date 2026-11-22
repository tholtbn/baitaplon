package com.auction.server.handler;

import com.auction.server.network.ClientSession;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class AuctionHandler {

    public JsonObject handleListAuctions(JsonObject req, ClientSession session) {
        // TODO: Gọi auctionService.listAuctions(...) và map danh sách DTO sang JsonArray
        JsonObject data = new JsonObject();
        data.add("auctions", new JsonArray());
        return data;
    }

    public JsonObject handleGetAuction(JsonObject req, ClientSession session) {
        String auctionId = req.has("auctionId") ? req.get("auctionId").getAsString() : null;
        if (auctionId == null || auctionId.isBlank()) {
            throw new IllegalArgumentException("auctionId is required");
        }

        // TODO: Gọi auctionService.getAuctionDetail(auctionId) và map DTO sang JsonObject
        JsonObject data = new JsonObject();
        data.addProperty("auctionId", auctionId);
        return data;
    }
}