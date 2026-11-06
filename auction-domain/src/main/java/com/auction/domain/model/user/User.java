package com.auction.domain.model.user;

import com.auction.contract.vocab.Role;
import com.auction.domain.model.Entity;
import com.auction.domain.model.Guard;

/** Người dùng hệ thống. Chỉ lưu mật khẩu đã băm, domain không bao giờ thấy mật khẩu thô. */
public abstract class User extends Entity {

    private final String username;
    private final String passwordHash;
    private final String email;

    protected User(String id, String username, String passwordHash, String email) {
        super(id);
        this.username = Guard.requireNotBlank(username, "username");
        this.passwordHash = Guard.requireNotBlank(passwordHash, "passwordHash");
        this.email = Guard.requireNotBlank(email, "email");
    }

    /** Vai trò của người dùng, do lớp con quyết định. */
    public abstract Role getRole();

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String printInfo() {
        return "[" + getRole() + "] " + username + " <" + email + ">";
    }
}