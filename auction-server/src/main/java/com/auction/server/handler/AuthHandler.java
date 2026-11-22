package com.auction.server.handler;

import com.auction.server.network.ClientSession;
import com.google.gson.JsonObject;

import java.util.Objects;
import java.util.UUID;

public class AuthHandler {
    private final Object authService; // Thay bằng AuthService thật khi tầng application ổn định

    public AuthHandler(Object authService) {
        this.authService = Objects.requireNonNull(authService, "authService must not be null");
    }

    public JsonObject handleLogin(JsonObject req, ClientSession session) {
        if (req == null || !req.has("username") || !req.has("password")) {
            throw new IllegalArgumentException("username and password are required");
        }
        String username = req.get("username").getAsString().trim();
        String password = req.get("password").getAsString();
        if (username.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("username and password must not be blank");
        }

        // TODO: Gọi AuthService để xác thực người dùng, ví dụ:
        // var user = ((AuthService) authService).login(username, password);
        String userId = "user-" + username;
        String token = UUID.randomUUID().toString();
        session.setAuthenticated(userId, token);

        JsonObject data = new JsonObject();
        data.addProperty("userId", userId);
        data.addProperty("username", username);
        data.addProperty("token", token);
        return data;
    }

    public JsonObject handleRegister(JsonObject req, ClientSession session) {
        if (req == null || !req.has("username") || !req.has("password")) {
            throw new IllegalArgumentException("username and password are required");
        }
        String username = req.get("username").getAsString().trim();
        String password = req.get("password").getAsString();
        String role = (req.has("role") && !req.get("role").isJsonNull())
                ? req.get("role").getAsString().trim()
                : "BIDDER";

        if (username.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("username and password must not be blank");
        }

        // TODO: Gọi AuthService đăng ký tài khoản, ví dụ:
        // var created = ((AuthService) authService).register(username, password, role);

        JsonObject data = new JsonObject();
        data.addProperty("username", username);
        data.addProperty("role", role);
        data.addProperty("registered", true);
        return data;
    }
}