package com.auction.server.dispatch;

import com.auction.server.network.ClientSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommandDispatcher {

    public enum MessageType {
        LOGIN,
        REGISTER,
        LIST_AUCTIONS,
        GET_AUCTION,
        CREATE_ITEM,
        PLACE_BID,
        SUBSCRIBE,
        UNSUBSCRIBE
    }

    private final Map<String, CommandHandler> handlers = new ConcurrentHashMap<>();

    public void register(String messageType, CommandHandler handler) {
        if (messageType != null && handler != null) {
            handlers.put(messageType, handler);
        }
    }

    public void register(MessageType messageType, CommandHandler handler) {
        if (messageType != null && handler != null) {
            handlers.put(messageType.name(), handler);
        }
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