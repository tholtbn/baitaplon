package com.auction.application.service;

import com.auction.application.port.out.UserRepository;
import com.auction.contract.dto.UserDto;
import com.auction.contract.vocab.Role;
import java.util.List;

public final class AdminApplicationService extends AccessSupport {
    public AdminApplicationService(UserRepository users) {
        super(users);
    }

    public List<UserDto> listUsers(String adminId) {
        requireRole(adminId, Role.ADMIN);
        return users.findAll().stream().map(DtoMapper::user).toList();
    }
}
