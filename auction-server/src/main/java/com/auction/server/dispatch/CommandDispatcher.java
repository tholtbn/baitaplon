package com.auction.server.dispatch;

import com.auction.server.network.ClientSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandDispatcher {
    private final Map<String, CommandHandler> handlers = new ConcurrentHashMap<>();

    public void register(String messageType, CommandHandler handler) {
        handlers.put(messageType, handler);
    }

    public void dispatch(String messageType, String requestJson, ClientSession session) {
        CommandHandler handler = handlers.get(messageType);
        if (handler != null) {
            handler.handle(requestJson, session);
        } else {
            session.sendResponse("{\"status\":\"ERROR\",\"message\":\"Unknown command type\"}");
        }
    }
}

interface CommandHandler {
    void handle(String requestJson, ClientSession session);
}