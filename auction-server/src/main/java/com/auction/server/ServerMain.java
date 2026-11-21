package com.auction.server;

import com.auction.infrastructure.config.AppConfig;
import com.auction.server.network.SocketServer;
import com.auction.server.network.SessionRegistry;
import com.auction.server.broadcast.EventBroadcaster;
import com.auction.infrastructure.event.InMemoryEventBus;

public class ServerMain {
    public static void main(String[] args) {
        try {
            AppConfig config = AppConfig.getInstance();
            SessionRegistry sessionRegistry = new SessionRegistry();

            // Đăng ký EventBroadcaster vào EventBus
            InMemoryEventBus eventBus = config.getEventBus();
            EventBroadcaster broadcaster = new EventBroadcaster(sessionRegistry);
            eventBus.register(broadcaster);

            int port = 8080;
            SocketServer server = new SocketServer(port, sessionRegistry);
            System.out.println("Auction Server đang khởi động tại cổng " + port + "...");
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}