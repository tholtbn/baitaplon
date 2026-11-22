package com.auction.server.handler;

import com.auction.server.network.ClientSession;
import com.google.gson.JsonObject;

import java.util.UUID;

public class AuthHandler {

    public JsonObject handleLogin(JsonObject req, ClientSession session) {
        String username = req.has("username") ? req.get("username").getAsString() : null;
        String password = req.has("password") ? req.get("password").getAsString() : null;

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("username and password are required");
        }

        // TODO: Gọi authService.login(username, password) để xác thực qua PasswordHasher + UserRepository
        // Sau khi xác thực thành công, sinh token ngẫu nhiên và gắn vào session:
        String userId = "user-" + username; // thay bằng user.getId() từ AuthService
        String token = UUID.randomUUID().toString();
        session.setAuthenticated(userId, token);

        JsonObject data = new JsonObject();
        data.addProperty("userId", userId);
        data.addProperty("username", username);
        data.addProperty("token", token);
        return data;
    }

    public JsonObject handleRegister(JsonObject req, ClientSession session) {
        String username = req.has("username") ? req.get("username").getAsString() : null;
        String password = req.has("password") ? req.get("password").getAsString() : null;
        String role = req.has("role") && !req.get("role").isJsonNull()
                ? req.get("role").getAsString()
                : "BIDDER";

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("username and password are required");
        }

        // TODO: Gọi authService.register(username, password, role)
        JsonObject data = new JsonObject();
        data.addProperty("username", username);
        data.addProperty("role", role);
        data.addProperty("registered", true);
        return data;
    }
}