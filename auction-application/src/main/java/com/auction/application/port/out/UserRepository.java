package com.auction.application.port.out;

import com.auction.domain.model.user.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(String id);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    User save(User user);
}
