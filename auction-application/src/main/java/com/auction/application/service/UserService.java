package com.auction.application.service;

import com.auction.application.exception.NotFoundException;
import com.auction.application.exception.UnauthorizedException;
import com.auction.application.port.UserRepository;
import com.auction.domain.model.user.User;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {

    private final UserRepository userRepository;

    private final Set<String> blockedUsers =
            ConcurrentHashMap.newKeySet();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> listUsers(User admin) {
        checkAdmin(admin);
        return userRepository.findAll();
    }

    public void blockUser(User admin, String userId) {
        checkAdmin(admin);
        findUser(userId);
        blockedUsers.add(userId);
    }

    public void unblockUser(User admin, String userId) {
        checkAdmin(admin);
        findUser(userId);
        blockedUsers.remove(userId);
    }

    public boolean isBlocked(String userId) {
        return blockedUsers.contains(userId);
    }

    private User findUser(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new NotFoundException("User not found: " + userId));
    }

    private void checkAdmin(User user) {
        if (user == null || !"ADMIN".equals(user.getRole().name())) {
            throw new UnauthorizedException("Admin permission required");
        }
    }
}