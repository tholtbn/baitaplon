package com.auction.domain.model.user;

import com.auction.contract.vocab.Role;

/** Người tham gia đấu giá. */
public class Bidder extends User {

    public Bidder(String id, String username, String passwordHash, String email) {
        super(id, username, passwordHash, email);
    }

    @Override
    public Role getRole() {
        return Role.BIDDER;
    }
}