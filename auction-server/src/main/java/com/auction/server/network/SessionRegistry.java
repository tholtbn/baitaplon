package com.auction.server.network;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SessionRegistry {
    private final ConcurrentHashMap<String, Set<ClientSession>> auctionSubscriptions = new ConcurrentHashMap<>();

    public void subscribe(String auctionId, ClientSession session) {
        auctionSubscriptions.computeIfAbsent(auctionId, k -> ConcurrentHashMap.newKeySet()).add(session);
        session.addSubscribedAuction(auctionId);
    }

    public void unsubscribe(String auctionId, ClientSession session) {
        Set<ClientSession> sessions = auctionSubscriptions.get(auctionId);
        if (sessions != null) {
            sessions.remove(session);
        }
        session.removeSubscribedAuction(auctionId);
    }

    public void removeSessionCompletely(ClientSession session) {
        for (String auctionId : session.getSubscribedAuctions()) {
            unsubscribe(auctionId, session);
        }
    }

    public Set<ClientSession> getSubscribers(String auctionId) {
        return auctionSubscriptions.getOrDefault(auctionId, Set.of());
    }
}