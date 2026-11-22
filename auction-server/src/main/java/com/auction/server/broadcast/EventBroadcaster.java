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
        if (event == null || sessionRegistry == null) {
            return;
        }

        if (event instanceof BidPlacedEvent bEvent) {
            String jsonPayload = toJsonPayload(bEvent);
            broadcastToAuction(bEvent.auctionId(), jsonPayload);
        }
        // Khi có thêm DomainEvent (ví dụ AuctionExtendedEvent, AuctionFinishedEvent),
        // thêm nhánh else if tương tự tại đây và gọi broadcastToAuction(auctionId, payload).
    }

    public void broadcastToAuction(String auctionId, String jsonPayload) {
        if (auctionId == null || auctionId.isBlank() || jsonPayload == null) {
            return;
        }
        Set<ClientSession> subscribers = sessionRegistry.getSubscribers(auctionId);
        if (subscribers == null || subscribers.isEmpty()) {
            return;
        }
        for (ClientSession session : subscribers) {
            if (session == null) {
                continue;
            }
            try {
                session.sendResponse(jsonPayload);
            } catch (Exception ignored) {
                // Tránh làm gián đoạn broadcast tới các client khác nếu một kết nối vừa đóng
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