package com.example.financery.service;

import com.example.financery.model.User;
import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    User createUser(User user);

    User getUserById(long id);

    User getUserByEmail(String email);

    User updateUser(long id, User user);

    void deleteUser(long id);
}
