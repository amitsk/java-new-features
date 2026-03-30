package com.example;

public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public String getUsername(int id) {
        return repo.findById(id)
                   .map(User::name)
                   .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    public void register(String name, String email) {
        // simplified: in real code, validate email, hash password, etc.
        repo.save(new User(0, name));
    }
}
