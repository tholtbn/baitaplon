package com.auction.infrastructure.repository;

import com.auction.application.port.UserRepository;
import com.auction.contract.vocab.Role;
import com.auction.domain.model.user.User;
import com.auction.infrastructure.dao.UserDao;
import com.auction.infrastructure.dao.UserDao.UserRow;

import java.util.List;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {
    private final UserDao userDao;

    public JdbcUserRepository(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Optional<User> findById(String userId) {
        return userDao.findById(userId).map(this::mapToDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userDao.findByUsername(username).map(this::mapToDomain);
    }

    @Override
    public List<User> findAll() {
        return userDao.findAll().stream()
                .map(this::mapToDomain)
                .toList();
    }

    @Override
    public void save(User user) {
        String roleName = user.getRole() != null ? user.getRole().name() : Role.BIDDER.name();
        UserRow row = new UserRow(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                roleName
        );
        userDao.insert(row);
    }

    protected User mapToDomain(UserRow row) {
        String email = row.username() + "@local.invalid";
        return new User(row.id(), row.username(), row.passwordHash(), email) {
            @Override
            public Role getRole() {
                return Role.valueOf(row.role());
            }
        };
    }
}