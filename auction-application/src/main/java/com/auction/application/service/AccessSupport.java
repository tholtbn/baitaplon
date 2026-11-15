package com.auction.application.service;

import com.auction.application.exception.ApplicationException;
import com.auction.application.port.out.UserRepository;
import com.auction.contract.vocab.ErrorCode;
import com.auction.contract.vocab.Role;
import com.auction.domain.model.user.User;

abstract class AccessSupport {
    protected final UserRepository users;

    protected AccessSupport(UserRepository users) {
        this.users = users;
    }

    protected User requireUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ApplicationException(ErrorCode.UNAUTHENTICATED, "Authentication required");
        }
        return users.findById(userId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.UNAUTHENTICATED, "User not found"));
    }

    protected User requireRole(String userId, Role role) {
        User user = requireUser(userId);
        if (user.getRole() != role) {
            throw new ApplicationException(ErrorCode.FORBIDDEN, "Insufficient permissions");
        }
        return user;
    }

    protected User requireSellerOrAdmin(String userId) {
        User user = requireUser(userId);
        if (user.getRole() != Role.SELLER && user.getRole() != Role.ADMIN) {
            throw new ApplicationException(ErrorCode.FORBIDDEN, "Seller or admin role required");
        }
        return user;
    }
}
