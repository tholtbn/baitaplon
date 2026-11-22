package com.auction.server.handler;

import com.auction.server.network.ClientSession;
import com.auction.server.network.SessionRegistry;
import com.google.gson.JsonObject;

import java.util.Objects;

public class SubscriptionHandler {
    private final SessionRegistry sessionRegistry;

    public SubscriptionHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = Objects.requireNonNull(sessionRegistry, "sessionRegistry must not be null");
    }

    public JsonObject handleSubscribe(JsonObject req, ClientSession session) {
        String auctionId = extractAuctionId(req);
        sessionRegistry.subscribe(auctionId, session);

        JsonObject data = new JsonObject();
        data.addProperty("auctionId", auctionId);
        data.addProperty("subscribed", true);
        return data;
    }

    public JsonObject handleUnsubscribe(JsonObject req, ClientSession session) {
        String auctionId = extractAuctionId(req);
        sessionRegistry.unsubscribe(auctionId, session);

        JsonObject data = new JsonObject();
        data.addProperty("auctionId", auctionId);
        data.addProperty("subscribed", false);
        return data;
    }

    private String extractAuctionId(JsonObject req) {
        if (req == null || !req.has("auctionId")) {
            throw new IllegalArgumentException("auctionId is required");
        }
        String auctionId = req.get("auctionId").getAsString().trim();
        if (auctionId.isEmpty()) {
            throw new IllegalArgumentException("auctionId must not be blank");
        }
        return auctionId;
    }
}