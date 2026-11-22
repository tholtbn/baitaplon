package com.auction.server;

import com.auction.infrastructure.config.AppConfig;
import com.auction.infrastructure.event.InMemoryEventBus;
import com.auction.server.broadcast.EventBroadcaster;
import com.auction.server.dispatch.CommandDispatcher;
import com.auction.server.handler.AuctionHandler;
import com.auction.server.handler.AuthHandler;
import com.auction.server.handler.BidHandler;
import com.auction.server.handler.ItemHandler;
import com.auction.server.handler.SubscriptionHandler;
import com.auction.server.network.SessionRegistry;
import com.auction.server.network.SocketServer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.time.Clock;

public class ServerMain {
    public static void main(String[] args) {
        try {
            Clock serverClock = Clock.systemUTC();
            AppConfig config = AppConfig.getInstance();
            SessionRegistry sessionRegistry = new SessionRegistry();
            CommandDispatcher dispatcher = new CommandDispatcher();

            // 1. Khởi tạo Service (thay bằng service thật của tầng application khi đồng bộ xong)
            Object authService = new Object();
            Object itemService = new Object();
            Object auctionService = new Object();
            Object biddingService = new Object();

            // 2. Nối EventBroadcaster với InMemoryEventBus
            InMemoryEventBus eventBus = config.getEventBus();
            EventBroadcaster broadcaster = new EventBroadcaster(sessionRegistry);
            registerBroadcaster(eventBus, broadcaster);

            // 3. Khởi tạo và đăng ký các Handler vào CommandDispatcher
            AuthHandler authHandler = new AuthHandler(authService);
            ItemHandler itemHandler = new ItemHandler(itemService);
            AuctionHandler auctionHandler = new AuctionHandler(auctionService);
            BidHandler bidHandler = new BidHandler(biddingService, serverClock);
            SubscriptionHandler subscriptionHandler = new SubscriptionHandler(sessionRegistry);

            dispatcher.register(CommandDispatcher.MessageType.LOGIN, (reqJson, session) ->
                    authHandler.handleLogin(parseJsonObject(reqJson), session));
            dispatcher.register(CommandDispatcher.MessageType.REGISTER, (reqJson, session) ->
                    authHandler.handleRegister(parseJsonObject(reqJson), session));
            dispatcher.register(CommandDispatcher.MessageType.CREATE_ITEM, (reqJson, session) ->
                    itemHandler.handleCreateItem(parseJsonObject(reqJson), session));
            dispatcher.register(CommandDispatcher.MessageType.LIST_AUCTIONS, (reqJson, session) ->
                    auctionHandler.handleListAuctions(parseJsonObject(reqJson), session));
            dispatcher.register(CommandDispatcher.MessageType.GET_AUCTION, (reqJson, session) ->
                    auctionHandler.handleGetAuction(parseJsonObject(reqJson), session));
            dispatcher.register(CommandDispatcher.MessageType.PLACE_BID, (reqJson, session) ->
                    bidHandler.handlePlaceBid(parseJsonObject(reqJson), session));
            dispatcher.register(CommandDispatcher.MessageType.SUBSCRIBE, (reqJson, session) ->
                    subscriptionHandler.handleSubscribe(parseJsonObject(reqJson), session));
            dispatcher.register(CommandDispatcher.MessageType.UNSUBSCRIBE, (reqJson, session) ->
                    subscriptionHandler.handleUnsubscribe(parseJsonObject(reqJson), session));

            int port = 8080;
            SocketServer server = new SocketServer(port, sessionRegistry);
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

            System.out.println("Auction Server đang khởi động tại cổng " + port + " (clock=" + serverClock.getZone() + ")...");
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void registerBroadcaster(InMemoryEventBus eventBus, EventBroadcaster broadcaster) {
        if (eventBus == null || broadcaster == null) {
            return;
        }
        eventBus.register(broadcaster);
        // Nếu InMemoryEventBus dùng tên phương thức khác (như subscribe / addListener),
        // đổi dòng trên thành tên phương thức tương ứng của InMemoryEventBus.
    }

    private static JsonObject parseJsonObject(String reqJson) {
        if (reqJson == null || reqJson.isBlank()) {
            return new JsonObject();
        }
        return JsonParser.parseString(reqJson).getAsJsonObject();
    }
}