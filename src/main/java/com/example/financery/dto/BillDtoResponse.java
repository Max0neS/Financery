package com.example.financery.dto;

import lombok.Data;

@Data
public class BillDtoResponse {

    private long id;
    private String name;
    private double balance;
    private long user_id;
}
