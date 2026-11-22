package com.auction.server.dispatch;

import com.auction.server.network.ClientSession;

@FunctionalInterface
public interface CommandHandler {
    void handle(String requestJson, ClientSession session);
}