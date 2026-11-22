package com.auction.server.handler;

import com.auction.server.network.ClientSession;
import com.auction.server.network.SessionRegistry;
import com.google.gson.JsonObject;

public class SubscriptionHandler {
    private final SessionRegistry sessionRegistry;

    public SubscriptionHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public JsonObject handleSubscribe(JsonObject req, ClientSession session) {
        String auctionId = req.has("auctionId") ? req.get("auctionId").getAsString() : null;
        if (auctionId == null || auctionId.isBlank()) {
            throw new IllegalArgumentException("auctionId is required");
        }

        sessionRegistry.subscribe(auctionId, session);

        JsonObject data = new JsonObject();
        data.addProperty("auctionId", auctionId);
        data.addProperty("subscribed", true);
        return data;
    }

    public JsonObject handleUnsubscribe(JsonObject req, ClientSession session) {
        String auctionId = req.has("auctionId") ? req.get("auctionId").getAsString() : null;
        if (auctionId == null || auctionId.isBlank()) {
            throw new IllegalArgumentException("auctionId is required");
        }

        sessionRegistry.unsubscribe(auctionId, session);

        JsonObject data = new JsonObject();
        data.addProperty("auctionId", auctionId);
        data.addProperty("subscribed", false);
        return data;
    }
}