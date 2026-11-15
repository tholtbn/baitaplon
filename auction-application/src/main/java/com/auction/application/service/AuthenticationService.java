package com.auction.application.service;

import com.auction.application.exception.ApplicationException;
import com.auction.application.port.out.IdGenerator;
import com.auction.application.port.out.PasswordHasher;
import com.auction.application.port.out.TokenService;
import com.auction.application.port.out.UserRepository;
import com.auction.contract.dto.UserDto;
import com.auction.contract.request.LoginRequest;
import com.auction.contract.request.RegisterRequest;
import com.auction.contract.response.LoginResponse;
import com.auction.contract.vocab.ErrorCode;
import com.auction.contract.vocab.Role;
import com.auction.domain.model.user.*;
import java.util.Locale;

public final class AuthenticationService {
    private final UserRepository users;
    private final PasswordHasher passwords;
    private final TokenService tokens;
    private final IdGenerator ids;

    public AuthenticationService(UserRepository users, PasswordHasher passwords,
                                 TokenService tokens, IdGenerator ids) {
        this.users = users;
        this.passwords = passwords;
        this.tokens = tokens;
        this.ids = ids;
    }

    public UserDto register(RegisterRequest request) {
        if (request == null || request.role() == null) {
            throw new ApplicationException(ErrorCode.BAD_REQUEST, "Registration data is required");
        }
        String username = required(request.username(), "Username");
        String password = required(request.password(), "Password");
        String email = required(request.email(), "Email");
        if (request.role() == Role.ADMIN) {
            throw new ApplicationException(ErrorCode.FORBIDDEN, "Admin accounts cannot be registered publicly");
        }
        if (users.findByUsername(username).isPresent()) {
            throw new ApplicationException(ErrorCode.CONFLICT, "Username already exists");
        }
        String hash = passwords.hash(password);
        User user = request.role() == Role.SELLER
                ? new Seller(ids.nextId(), username, hash, email)
                : new Bidder(ids.nextId(), username, hash, email);
        return DtoMapper.user(users.save(user));
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null) throw new ApplicationException(ErrorCode.BAD_REQUEST, "Login data is required");
        String username = required(request.username(), "Username");
        String password = required(request.password(), "Password");
        User user = users.findByUsername(username)
                .orElseThrow(() -> new ApplicationException(ErrorCode.UNAUTHENTICATED, "Invalid username or password"));
        if (!passwords.matches(password, user.getPasswordHash())) {
            throw new ApplicationException(ErrorCode.UNAUTHENTICATED, "Invalid username or password");
        }
        return new LoginResponse(tokens.issue(user.getId()), DtoMapper.user(user));
    }

    public void logout(String token) {
        if (token == null || token.isBlank()) {
            throw new ApplicationException(ErrorCode.UNAUTHENTICATED, "Token is required");
        }
        tokens.revoke(token);
    }

    public UserDto authenticate(String token) {
        if (token == null || token.isBlank()) {
            throw new ApplicationException(ErrorCode.UNAUTHENTICATED, "Token is required");
        }
        String userId = tokens.userIdFromToken(token)
                .orElseThrow(() -> new ApplicationException(ErrorCode.UNAUTHENTICATED, "Invalid or expired token"));
        User user = users.findById(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.UNAUTHENTICATED, "User not found"));
        return DtoMapper.user(user);
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ApplicationException(ErrorCode.VALIDATION, field + " must not be blank");
        }
        return value.trim();
    }
}
