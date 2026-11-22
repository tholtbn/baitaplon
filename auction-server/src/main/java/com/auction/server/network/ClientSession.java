package com.auction.server.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClientSession implements Runnable {
    private final Socket socket;
    private final SessionRegistry sessionRegistry;
    private PrintWriter writer;
    private String userId;
    private String token;
    private final Set<String> subscribedAuctions = Collections.synchronizedSet(new HashSet<>());

    public ClientSession(Socket socket, SessionRegistry sessionRegistry) {
        this.socket = socket;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                // Dispatch request tại đây hoặc gọi CommandDispatcher
            }
        } catch (Exception e) {
            // Mất kết nối
        } finally {
            sessionRegistry.removeSessionCompletely(this);
            try { socket.close(); } catch (Exception ignored) {}
        }
    }

    public synchronized void sendResponse(String jsonResponse) {
        if (writer != null) {
            writer.println(jsonResponse);
        }
    }

    public void addSubscribedAuction(String auctionId) { subscribedAuctions.add(auctionId); }
    public void removeSubscribedAuction(String auctionId) { subscribedAuctions.remove(auctionId); }
    public Set<String> getSubscribedAuctions() { return new HashSet<>(subscribedAuctions); }

    public void setUserId(String userId) { this.userId = userId; }
    public String getUserId() { return userId; }
    public void setToken(String token) { this.token = token; }
    public String getToken() { return token; }

    public void setAuthenticated(String userId, String token) {
    }
}