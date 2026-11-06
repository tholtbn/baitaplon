package com.auction.domain.model.user;

import com.auction.contract.vocab.Role;

/** Người bán đăng sản phẩm đấu giá. */
public class Seller extends User {

    public Seller(String id, String username, String passwordHash, String email) {
        super(id, username, passwordHash, email);
    }

    @Override
    public Role getRole() {
        return Role.SELLER;
    }
}