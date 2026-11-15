package com.auction.application.service;

import com.auction.application.exception.ApplicationException;
import com.auction.application.port.out.TokenService;
import com.auction.application.port.out.UserRepository;
import com.auction.contract.dto.UserDto;
import com.auction.contract.vocab.ErrorCode;

public final class SessionService {
    private final TokenService tokens;
    private final UserRepository users;

    public SessionService(TokenService tokens, UserRepository users) {
        this.tokens = tokens;
        this.users = users;
    }

    public UserDto currentUser(String token) {
        if (token == null || token.isBlank()) {
            throw new ApplicationException(ErrorCode.UNAUTHENTICATED, "Token is required");
        }
        String userId = tokens.userIdFromToken(token)
                .orElseThrow(() -> new ApplicationException(ErrorCode.UNAUTHENTICATED, "Invalid or expired token"));
        return users.findById(userId)
                .map(DtoMapper::user)
                .orElseThrow(() -> new ApplicationException(ErrorCode.UNAUTHENTICATED, "User not found"));
    }
}
