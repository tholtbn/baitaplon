package com.auction.domain.model.user;

import com.auction.contract.vocab.Role;

/** Quản trị viên hệ thống. */
public class Admin extends User {

    public Admin(String id, String username, String passwordHash, String email) {
        super(id, username, passwordHash, email);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }
}