package com.auction.contract.protocol;

import com.auction.contract.json.Json;
import com.auction.contract.vocab.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;

public record Response(String requestId, boolean success, JsonNode payload,
                       ErrorCode error, String message) {

    public static Response ok(String requestId, Object payload) {
        JsonNode node = payload == null ? null : Json.mapper().valueToTree(payload);
        return new Response(requestId, true, node, null, null);
    }

    public static Response error(String requestId, ErrorCode code, String message) {
        return new Response(requestId, false, null, code, message);
    }

    public <T> T payloadAs(Class<T> type) {
        if (payload == null) {
            throw new IllegalArgumentException("payload is missing");
        }
        return Json.mapper().convertValue(payload, type);
    }
}