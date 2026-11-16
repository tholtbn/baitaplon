package com.auction.application.port;

import com.auction.domain.model.user.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(String userId);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    void save(User user);
}
