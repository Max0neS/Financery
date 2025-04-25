package com.example.financery.service.impl;

import com.example.financery.model.User;
import com.example.financery.repository.UserRepository;
import com.example.financery.service.UserService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getUserById(long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User updateUser(long id, User user) {
        User newUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id " + id));

        newUser.setName(user.getName());
        newUser.setEmail(user.getEmail());

        return userRepository.save(newUser);
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }
}
