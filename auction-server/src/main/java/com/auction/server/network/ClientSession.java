package com.auction.server.network;

import com.auction.server.dispatch.CommandDispatcher;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
    private final CommandDispatcher dispatcher;
    private PrintWriter writer;
    private String userId;
    private String token;
    private final Set<String> subscribedAuctions = Collections.synchronizedSet(new HashSet<>());

    public ClientSession(Socket socket, SessionRegistry sessionRegistry) {
        this(socket, sessionRegistry, null);
    }

    public ClientSession(Socket socket, SessionRegistry sessionRegistry, CommandDispatcher dispatcher) {
        this.socket = socket;
        this.sessionRegistry = sessionRegistry;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                if (inputLine.isBlank()) {
                    continue;
                }
                handleIncomingLine(inputLine.trim());
            }
        } catch (Exception e) {
            // Mất kết nối hoặc lỗi I/O của client
        } finally {
            sessionRegistry.removeSessionCompletely(this);
            try {
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }

    private void handleIncomingLine(String rawLine) {
        if (dispatcher == null) {
            sendResponse("{\"status\":\"ERROR\",\"message\":\"Dispatcher not configured\"}");
            return;
        }
        try {
            JsonObject root = JsonParser.parseString(rawLine).getAsJsonObject();
            String messageType = extractMessageType(root);
            if (messageType == null || messageType.isBlank()) {
                sendResponse("{\"status\":\"ERROR\",\"message\":\"Missing message type\"}");
                return;
            }

            JsonElement payloadElement = root.has("data") && !root.get("data").isJsonNull()
                    ? root.get("data")
                    : root;
            String requestJson = payloadElement.toString();

            dispatcher.dispatch(messageType.trim(), requestJson, this);
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage().replace("\"", "'") : "Invalid request";
            sendResponse("{\"status\":\"ERROR\",\"message\":\"" + msg + "\"}");
        }
    }

    private String extractMessageType(JsonObject root) {
        if (root.has("type") && !root.get("type").isJsonNull()) {
            return root.get("type").getAsString();
        }
        if (root.has("messageType") && !root.get("messageType").isJsonNull()) {
            return root.get("messageType").getAsString();
        }
        return null;
    }

    public synchronized void sendResponse(String jsonResponse) {
        if (writer != null) {
            writer.println(jsonResponse);
        }
    }

    public void addSubscribedAuction(String auctionId) {
        subscribedAuctions.add(auctionId);
    }

    public void removeSubscribedAuction(String auctionId) {
        subscribedAuctions.remove(auctionId);
    }

    public Set<String> getSubscribedAuctions() {
        return new HashSet<>(subscribedAuctions);
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setAuthenticated(String userId, String token) {
        this.userId = userId;
        this.token = token;
    }
}