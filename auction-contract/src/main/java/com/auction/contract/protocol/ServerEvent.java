package com.auction.contract.protocol;

import com.auction.contract.json.Json;
import com.fasterxml.jackson.databind.JsonNode;

/** Tin server chủ động đẩy cho client (không có requestId). */
public record ServerEvent(EventType eventType, JsonNode payload) {

    public static ServerEvent of(EventType type, Object payload) {
        return new ServerEvent(type, Json.mapper().valueToTree(payload));
    }

    public <T> T payloadAs(Class<T> type) {
        return Json.mapper().convertValue(payload, type);
    }
}