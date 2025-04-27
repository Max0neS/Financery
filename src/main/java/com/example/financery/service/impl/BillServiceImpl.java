package com.example.financery.service.impl;

import com.example.financery.dto.BillDtoRequest;
import com.example.financery.dto.BillDtoResponse;
import com.example.financery.mapper.BillMapper;
import com.example.financery.model.Bill;
import com.example.financery.model.User;
import com.example.financery.repository.BillRepository;
import com.example.financery.repository.UserRepository;
import com.example.financery.service.BillService;
import lombok.AllArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BillServiceImpl implements BillService {

    public static final String TASK_WITH_ID_NOT_FOUND = "Задача с id %d не найдена";

    private final BillRepository billRepository;
    private final BillMapper billMapper;
    private final UserRepository userRepository;

    @Override
    public List<BillDtoResponse> getAllBills() {
        List<BillDtoResponse> billsResponse = new ArrayList<>();
        billRepository.findAll().forEach(
                bill -> billsResponse.add(billMapper.toBillDto(bill)));
        return billsResponse;
    }

    @Override
    public List<BillDtoResponse> getBillsByUserId(long user_id) {
        List<Bill> bills = billRepository.findByUser(user_id);
        List<BillDtoResponse> billsResponse = new ArrayList<>();

        bills.forEach(bill -> billsResponse.add(billMapper.toBillDto(bill)));
        return billsResponse;
    }

    public BillDtoResponse getBillById(long id) {
        Bill bill = billRepository.findById(id).
                orElseThrow(() -> new RuntimeException(String.format(TASK_WITH_ID_NOT_FOUND, id)));
        return billMapper.toBillDto(bill);
    }

    @Override
    public BillDtoResponse createBill(BillDtoRequest billDto) {
        User user = userRepository.findById(billDto.getUser_id())
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        Bill bill = BillMapper.toBill(billDto);

        user.setBalance(user.getBalance() + bill.getBalance());
        bill.setUser(user);
        billRepository.save(bill);

        return billMapper.toBillDto(bill);
    }

    @Override
    public BillDtoResponse updateBill(long billId, BillDtoRequest billDto) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + billId));

        User user = userRepository.findById(bill.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found with id " + bill.getUser().getId()));

        double currentBalance = bill.getBalance();

        bill.setName(billDto.getName());
        bill.setBalance(billDto.getBalance());

        double newBalance = user.getBalance() - currentBalance + billDto.getBalance();
        user.setBalance(newBalance);

        userRepository.save(user);
        billRepository.save(bill);

        return billMapper.toBillDto(bill);
    }

    public void deleteBill(long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new RuntimeException(String.format(TASK_WITH_ID_NOT_FOUND, billId)));

        User user = userRepository.findById(bill.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found with id " + bill.getUser().getId()));

        user.setBalance(user.getBalance() - bill.getBalance());

        userRepository.save(user);
        billRepository.deleteById(billId);
    }

}
