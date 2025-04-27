package com.example.financery.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

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
    private List<Long> tagIds;

}
