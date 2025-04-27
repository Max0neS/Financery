package com.example.financery.service;

import com.example.financery.dto.UserDtoRequest;
import com.example.financery.dto.UserDtoResponse;
import com.example.financery.model.User;
import java.util.List;

public interface UserService {
    List<UserDtoResponse> getAllUsers();

    UserDtoResponse createUser(UserDtoRequest userDtoRequest);

    User getUserById(long id);

    User getUserByEmail(String email);

    UserDtoResponse updateUser(long id, UserDtoRequest userDtoRequest);

    void deleteUser(long id);
}
