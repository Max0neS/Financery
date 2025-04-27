package com.example.financery.dto;

import lombok.Data;

@Data
public class BillDtoRequest {

    private String name;
    private double balance;
    private long userId;
}
