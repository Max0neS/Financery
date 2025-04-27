package com.example.financery.controller;


import com.example.financery.dto.UserDtoRequest;
import com.example.financery.dto.UserDtoResponse;
import com.example.financery.model.User;
import com.example.financery.service.UserService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/get-all-users")
    public List<UserDtoResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/search-by-id/{id}")
    public UserDtoResponse getUserById(@PathVariable long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/search-by-email/{email}")
    public User getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }

    @PostMapping("/create")
    public UserDtoResponse createUser(@RequestBody UserDtoRequest userDtoRequest) {
        return userService.createUser(userDtoRequest);
    }

    @PutMapping("/update-by-id/{id}")
    public UserDtoResponse updateUser(
            @PathVariable long id,
            @RequestBody UserDtoRequest userDtoRequest) {
        return userService.updateUser(id, userDtoRequest);
    }

    @DeleteMapping("/delete-by-id/{id}")
    public void deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
    }
}
