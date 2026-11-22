package com.auction.server.network;

import org.junit.jupiter.api.Test;

import java.net.Socket;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SessionRegistryTest {

    @Test
    void subscribeAndUnsubscribeShouldManageAuctionSubscribers() {
        SessionRegistry registry = new SessionRegistry();
        ClientSession s1 = new ClientSession(new Socket(), registry);
        ClientSession s2 = new ClientSession(new Socket(), registry);

        registry.subscribe("auction-1", s1);
        registry.subscribe("auction-1", s2);

        Set<ClientSession> subscribers = registry.getSubscribers("auction-1");
        assertNotNull(subscribers);
        assertEquals(2, subscribers.size());
        assertTrue(subscribers.contains(s1));
        assertTrue(subscribers.contains(s2));

        registry.unsubscribe("auction-1", s1);
        subscribers = registry.getSubscribers("auction-1");
        assertEquals(1, subscribers.size());
        assertFalse(subscribers.contains(s1));
        assertTrue(subscribers.contains(s2));

        registry.removeSessionCompletely(s2);
        subscribers = registry.getSubscribers("auction-1");
        assertTrue(subscribers == null || subscribers.isEmpty());
    }
}