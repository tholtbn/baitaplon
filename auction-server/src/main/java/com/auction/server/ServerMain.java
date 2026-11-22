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
import com.google.gson.JsonParser;

import java.time.Clock;

public class ServerMain {
    public static void main(String[] args) {
        try {
            Clock serverClock = Clock.systemUTC();
            AppConfig config = AppConfig.getInstance();
            SessionRegistry sessionRegistry = new SessionRegistry();
            CommandDispatcher dispatcher = new CommandDispatcher();

            InMemoryEventBus eventBus = config.getEventBus();
            EventBroadcaster broadcaster = new EventBroadcaster(sessionRegistry);

            AuthHandler authHandler = new AuthHandler();
            ItemHandler itemHandler = new ItemHandler();
            AuctionHandler auctionHandler = new AuctionHandler();
            BidHandler bidHandler = new BidHandler();
            SubscriptionHandler subscriptionHandler = new SubscriptionHandler(sessionRegistry);

            dispatcher.register(CommandDispatcher.MessageType.LOGIN, (reqJson, session) ->
                    authHandler.handleLogin(JsonParser.parseString(reqJson).getAsJsonObject(), session));
            dispatcher.register(CommandDispatcher.MessageType.REGISTER, (reqJson, session) ->
                    authHandler.handleRegister(JsonParser.parseString(reqJson).getAsJsonObject(), session));
            dispatcher.register(CommandDispatcher.MessageType.CREATE_ITEM, (reqJson, session) ->
                    itemHandler.handleCreateItem(JsonParser.parseString(reqJson).getAsJsonObject(), session));
            dispatcher.register(CommandDispatcher.MessageType.LIST_AUCTIONS, (reqJson, session) ->
                    auctionHandler.handleListAuctions(JsonParser.parseString(reqJson).getAsJsonObject(), session));
            dispatcher.register(CommandDispatcher.MessageType.GET_AUCTION, (reqJson, session) ->
                    auctionHandler.handleGetAuction(JsonParser.parseString(reqJson).getAsJsonObject(), session));
            dispatcher.register(CommandDispatcher.MessageType.PLACE_BID, (reqJson, session) ->
                    bidHandler.handlePlaceBid(JsonParser.parseString(reqJson).getAsJsonObject(), session));
            dispatcher.register(CommandDispatcher.MessageType.SUBSCRIBE, (reqJson, session) ->
                    subscriptionHandler.handleSubscribe(JsonParser.parseString(reqJson).getAsJsonObject(), session));
            dispatcher.register(CommandDispatcher.MessageType.UNSUBSCRIBE, (reqJson, session) ->
                    subscriptionHandler.handleUnsubscribe(JsonParser.parseString(reqJson).getAsJsonObject(), session));

            int port = 8080;
            SocketServer server = new SocketServer(port, sessionRegistry);
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

            System.out.println("Auction Server đang khởi động tại cổng " + port + " (clock=" + serverClock.getZone() + ")...");
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}