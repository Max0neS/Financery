package com.example.financery.controller;

import com.example.financery.dto.BillDtoRequest;
import com.example.financery.dto.BillDtoResponse;
import com.example.financery.dto.TransactionDtoRequest;
import com.example.financery.dto.TransactionDtoResponse;
import com.example.financery.service.BillService;
import com.example.financery.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@AllArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/get-all-transactions")
    public List<TransactionDtoResponse> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/get-transaction-by-id/{transactionId}")
    public ResponseEntity<TransactionDtoResponse> getTransactionById(@PathVariable long transactionId) {
        return ResponseEntity.ok(transactionService.getTransactionById(transactionId));
    }

    @GetMapping("/get-all-user-transactions/{userId}")
    public List<TransactionDtoResponse> getAllUserTransactions(@PathVariable long userId) {
        return transactionService.getTransactionsByUserId(userId);
    }

    @GetMapping("/get-all-bill-transactions/{billId}")
    public List<TransactionDtoResponse> getAllBillTransactions(@PathVariable long billId) {
        return transactionService.getTransactionsByBillId(billId);
    }


    @PostMapping("/create")
    public TransactionDtoResponse createBill(@RequestBody TransactionDtoRequest transactionDto) {
        TransactionDtoResponse createTransaction = transactionService.createTransaction(transactionDto);
        return createTransaction;
    }

    @PutMapping("/update-by-id/{transactionId}")
    public TransactionDtoResponse updateTransactionById(
            @PathVariable long transactionId,
            @RequestBody TransactionDtoRequest transasctionDto) {
        return transactionService.updateTransaction(transactionId, transasctionDto);
    }

    @DeleteMapping("/delete-by-id/{transactionId}")
    public void deleteTransactionById(@PathVariable long transactionId) {
        transactionService.deleteTransaction(transactionId);
    }
}
