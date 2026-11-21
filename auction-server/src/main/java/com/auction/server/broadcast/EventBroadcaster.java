package com.auction.server.broadcast;

import com.auction.application.event.BidPlacedEvent;
import com.auction.application.event.DomainEvent;
import com.auction.application.event.EventListener;
import com.auction.server.network.ClientSession;
import com.auction.server.network.SessionRegistry;

import java.util.Set;

public class EventBroadcaster implements EventListener<DomainEvent> {
    private final SessionRegistry sessionRegistry;

    public EventBroadcaster(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void on(DomainEvent event) {
        if (event instanceof BidPlacedEvent bEvent) {
            String jsonPayload = toJsonPayload(bEvent);
            Set<ClientSession> subscribers = sessionRegistry.getSubscribers(bEvent.auctionId());
            if (subscribers != null) {
                for (ClientSession session : subscribers) {
                    session.sendResponse(jsonPayload);
                }
            }
        }
    }

    private String toJsonPayload(BidPlacedEvent event) {
        String leader = event.leaderId() != null ? "\"" + escapeJson(event.leaderId()) + "\"" : "null";
        String endTime = event.endTime() != null ? "\"" + escapeJson(event.endTime().toString()) + "\"" : "null";
        String occurredAt = event.occurredAt() != null ? "\"" + escapeJson(event.occurredAt().toString()) + "\"" : "null";
        String currentPrice = event.currentPrice() != null ? event.currentPrice().toPlainString() : "null";

        return "{"
                + "\"type\":\"BID_PLACED\","
                + "\"auctionId\":\"" + escapeJson(event.auctionId()) + "\","
                + "\"currentPrice\":" + currentPrice + ","
                + "\"leaderId\":" + leader + ","
                + "\"endTime\":" + endTime + ","
                + "\"occurredAt\":" + occurredAt
                + "}";
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}