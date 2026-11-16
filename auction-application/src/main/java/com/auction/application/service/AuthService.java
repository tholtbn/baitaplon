package com.auction.application.service;

import com.auction.application.exception.DuplicateUserException;
import com.auction.application.port.IdGenerator;
import com.auction.application.port.PasswordHasher;
import com.auction.application.port.TokenService;
import com.auction.application.port.UserRepository;
import com.auction.domain.model.user.Bidder;
import com.auction.domain.model.user.User;

public class AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TokenService tokenService;
    private final IdGenerator idGenerator;

    public AuthService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            TokenService tokenService,
            IdGenerator idGenerator) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.tokenService = tokenService;
        this.idGenerator = idGenerator;
    }

    public User register(String username, String rawPassword, String email) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new DuplicateUserException("Username already exists");
        }

        String passwordHash = passwordHasher.hash(rawPassword);

        User user = new Bidder(
                idGenerator.newId(),
                username,
                passwordHash,
                email
        );

        userRepository.save(user);
        return user;
    }

    public String login(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid username or password"));

        if (!passwordHasher.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return tokenService.issue(user.getId());
    }

    public void logout(String token) {
        tokenService.revoke(token);
    }
}