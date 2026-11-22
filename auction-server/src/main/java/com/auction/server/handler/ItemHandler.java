package com.auction.server.handler;

import com.auction.server.network.ClientSession;
import com.google.gson.JsonObject;

import java.math.BigDecimal;

public class ItemHandler {

    public JsonObject handleCreateItem(JsonObject req, ClientSession session) {
        String actorId = session.getUserId();
        String name = req.has("name") ? req.get("name").getAsString() : null;
        String description = req.has("description") && !req.get("description").isJsonNull()
                ? req.get("description").getAsString()
                : "";
        BigDecimal startingPrice = req.has("startingPrice")
                ? req.get("startingPrice").getAsBigDecimal()
                : null;
        String type = req.has("type") ? req.get("type").getAsString() : null;

        if (name == null || name.isBlank() || startingPrice == null || type == null || type.isBlank()) {
            throw new IllegalArgumentException("name, startingPrice, and type are required");
        }

        // TODO: Gọi itemService.createItem(actorId, name, description, startingPrice, type)
        JsonObject data = new JsonObject();
        data.addProperty("sellerId", actorId);
        data.addProperty("name", name);
        data.addProperty("startingPrice", startingPrice);
        data.addProperty("type", type);
        return data;
    }
}