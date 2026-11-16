package com.auction.application.port;

import java.util.Optional;

public interface TokenService {
    String issue(String userId);
    Optional<String> userIdOf(String token);
    void revoke(String token);
}
