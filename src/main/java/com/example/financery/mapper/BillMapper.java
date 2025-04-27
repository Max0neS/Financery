package com.example.financery.mapper;

import com.example.financery.dto.BillDtoRequest;
import com.example.financery.dto.BillDtoResponse;
import com.example.financery.model.Bill;
import org.springframework.stereotype.Component;

@Component
public class BillMapper {

    public BillDtoResponse toBillDto(Bill bill) {
        BillDtoResponse billDtoResponse = new BillDtoResponse();

        billDtoResponse.setId(bill.getId());
        billDtoResponse.setName(bill.getName());
        billDtoResponse.setBalance(bill.getBalance());
        billDtoResponse.setUserId(bill.getUser().getId());

        return billDtoResponse;
    }

    public static Bill toBill(BillDtoRequest billDtoRequest) {
        Bill bill = new Bill();

        bill.setName(billDtoRequest.getName());
        bill.setBalance(billDtoRequest.getBalance());

        return bill;
    }
}
