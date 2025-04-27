package com.example.financery.dto;

import lombok.Data;

@Data
public class UserDtoRequest {

    private String name;
    private String email;

    private double balance;
}
