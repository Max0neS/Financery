package com.example.financery.service.impl;

import com.example.financery.dto.UserDtoRequest;
import com.example.financery.dto.UserDtoResponse;
import com.example.financery.mapper.UserMapper;
import com.example.financery.model.User;
import com.example.financery.repository.UserRepository;
import com.example.financery.service.UserService;

import java.util.ArrayList;
import java.util.List;

import com.example.financery.utils.InMemoryCache;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final InMemoryCache cache;

    @Override
    public List<UserDtoResponse> getAllUsers() {
        List<UserDtoResponse> userDtoResponses = new ArrayList<>();
        userRepository.findAll().forEach(user -> {
            Hibernate.initialize(user.getBills());
            userDtoResponses.add(userMapper.toDto(user));
        });
        return userDtoResponses;

    }

    @Override
    public UserDtoResponse createUser(UserDtoRequest userDtoRequest) {
        User user = userMapper.toEntity(userDtoRequest);
        user.setBalance(0.0);
        userRepository.save(user);
        return userMapper.toDto(user);
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
    public UserDtoResponse updateUser(long id, UserDtoRequest userDtoRequest) {
        User newUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id " + id));


        newUser.setName(userDtoRequest.getName());
        newUser.setEmail(userDtoRequest.getEmail());

        userRepository.save(newUser);

        return userMapper.toDto(newUser);
    }

    @Override
    public void deleteUser(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        userRepository.deleteById(id);

        cache.clearForUser(user.getId());
    }
}
