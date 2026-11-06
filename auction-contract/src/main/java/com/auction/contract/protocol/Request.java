package com.auction.contract.protocol;

import com.auction.contract.json.Json;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

public record Request(String requestId, MessageType type, String token, JsonNode payload) {

    public static Request of(MessageType type, String token, Object payload) {
        JsonNode node = payload == null ? null : Json.mapper().valueToTree(payload);
        return new Request(UUID.randomUUID().toString(), type, token, node);
    }

    /** Đọc payload thành đối tượng; ném IllegalArgumentException nếu sai định dạng. */
    public <T> T payloadAs(Class<T> type) {
        if (payload == null) {
            throw new IllegalArgumentException("payload is missing");
        }
        return Json.mapper().convertValue(payload, type);
    }
}