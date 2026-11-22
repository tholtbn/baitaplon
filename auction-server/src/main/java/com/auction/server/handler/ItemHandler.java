package com.auction.server.handler;

import com.auction.server.network.ClientSession;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.util.Objects;

public class ItemHandler {
    private final Object itemService; // Thay bằng ItemService thật khi tầng application ổn định

    public ItemHandler(Object itemService) {
        this.itemService = Objects.requireNonNull(itemService, "itemService must not be null");
    }

    public JsonObject handleCreateItem(JsonObject req, ClientSession session) {
        String sellerId = requireAuthenticatedUserId(session);
        if (req == null || !req.has("name") || !req.has("startingPrice") || !req.has("type")) {
            throw new IllegalArgumentException("name, startingPrice, and type are required");
        }

        String name = req.get("name").getAsString().trim();
        String description = (req.has("description") && !req.get("description").isJsonNull())
                ? req.get("description").getAsString()
                : "";
        BigDecimal startingPrice = req.get("startingPrice").getAsBigDecimal();
        String type = req.get("type").getAsString().trim();

        if (name.isEmpty() || type.isEmpty() || startingPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid item fields");
        }

        // TODO: Gọi ItemService tạo item, ví dụ:
        // var item = ((ItemService) itemService).createItem(sellerId, name, description, startingPrice, type);

        JsonObject data = new JsonObject();
        data.addProperty("sellerId", sellerId);
        data.addProperty("name", name);
        data.addProperty("description", description);
        data.addProperty("startingPrice", startingPrice);
        data.addProperty("type", type);
        return data;
    }

    private String requireAuthenticatedUserId(ClientSession session) {
        String userId = session.getUserId();
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("Session is not authenticated");
        }
        return userId;
    }
}