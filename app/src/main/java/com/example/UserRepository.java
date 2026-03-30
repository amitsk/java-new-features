package com.example;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(int id);
    void save(User user);
}
