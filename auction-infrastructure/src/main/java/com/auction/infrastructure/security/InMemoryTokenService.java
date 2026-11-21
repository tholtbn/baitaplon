package com.auction.infrastructure.security;

import com.auction.application.port.TokenService;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTokenService implements TokenService {
    private final Map<String, String> tokenToUserId = new ConcurrentHashMap<>();

    @Override
    public String issue(String userId) {
        String validUserId = Objects.requireNonNull(userId, "userId must not be null");
        String token = UUID.randomUUID().toString();
        tokenToUserId.put(token, validUserId);
        return token;
    }

    @Override
    public Optional<String> userIdOf(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(tokenToUserId.get(token));
    }

    @Override
    public void revoke(String token) {
        if (token != null) {
            tokenToUserId.remove(token);
        }
    }
}