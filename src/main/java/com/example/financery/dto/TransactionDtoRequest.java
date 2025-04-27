package com.example.financery.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TransactionDtoRequest {

    private String name;
    private String description;
    private boolean type;
    private double amount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
    private LocalDate date;

    private long userId;
    private long billId;
}
