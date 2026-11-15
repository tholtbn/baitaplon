package com.auction.application.port.out;

import java.util.Optional;

public interface TokenService {
    String issue(String userId);
    Optional<String> userIdFromToken(String token);
    void revoke(String token);
}
