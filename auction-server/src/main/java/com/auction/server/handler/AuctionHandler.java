package com.auction.server.handler;

import com.auction.server.network.ClientSession;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Objects;

public class AuctionHandler {
    private final Object auctionService; // Thay bằng AuctionService thật khi tầng application ổn định

    public AuctionHandler(Object auctionService) {
        this.auctionService = Objects.requireNonNull(auctionService, "auctionService must not be null");
    }

    public JsonObject handleListAuctions(JsonObject req, ClientSession session) {
        // TODO: Gọi AuctionService lấy danh sách phiên đấu giá và map sang JsonArray
        // var list = ((AuctionService) auctionService).listAuctions();

        JsonObject data = new JsonObject();
        data.add("auctions", new JsonArray());
        return data;
    }

    public JsonObject handleGetAuction(JsonObject req, ClientSession session) {
        if (req == null || !req.has("auctionId")) {
            throw new IllegalArgumentException("auctionId is required");
        }
        String auctionId = req.get("auctionId").getAsString().trim();
        if (auctionId.isEmpty()) {
            throw new IllegalArgumentException("auctionId must not be blank");
        }

        // TODO: Gọi AuctionService lấy chi tiết phiên đấu giá, ví dụ:
        // var detail = ((AuctionService) auctionService).getAuction(auctionId);

        JsonObject data = new JsonObject();
        data.addProperty("auctionId", auctionId);
        return data;
    }
}