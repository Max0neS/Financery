package com.example.financery.service.impl;

import com.example.financery.dto.BillDtoResponse;
import com.example.financery.dto.TransactionDtoRequest;
import com.example.financery.dto.TransactionDtoResponse;
import com.example.financery.mapper.TransactionMapper;
import com.example.financery.model.Bill;
import com.example.financery.model.Transaction;
import com.example.financery.model.User;
import com.example.financery.repository.BillRepository;
import com.example.financery.repository.TransactionRepository;
import com.example.financery.repository.UserRepository;
import com.example.financery.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    public static final String TRANSACTION_WITH_ID_NOT_FOUND = "Транзакция с id %d не найдена";

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final UserRepository userRepository;
    private final BillRepository billRepository;

    @Override
    public List<TransactionDtoResponse> getAllTransactions() {
        List<TransactionDtoResponse> transactionsResponse = new ArrayList<>();
        transactionRepository.findAll().forEach(
                transaction -> transactionsResponse.add(transactionMapper.toTransactionDto(transaction)));
        return transactionsResponse;
    }

    @Override
    public TransactionDtoResponse getTransactionById(long transactionId){
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException(
                        String.format(TRANSACTION_WITH_ID_NOT_FOUND, transactionId)));
        return transactionMapper.toTransactionDto(transaction);
    }

    @Override
    public List<TransactionDtoResponse> getTransactionsByUserId(long userId){
        List<Transaction> transactions = transactionRepository.findByUser(userId);
        List<TransactionDtoResponse> transactionsResponse = new ArrayList<>();

        transactions.forEach(
                transaction -> transactionsResponse
                        .add(transactionMapper
                                .toTransactionDto(transaction)));
        return transactionsResponse;
    }

    @Override
    public List<TransactionDtoResponse> getTransactionsByBillId(long billId){
        List<Transaction> transactions = transactionRepository.findByBill(billId);
        List<TransactionDtoResponse> transactionsResponse = new ArrayList<>();

        transactions.forEach(
                transaction -> transactionsResponse
                        .add(transactionMapper
                                .toTransactionDto(transaction)));
        return transactionsResponse;
    }

    @Override
    public TransactionDtoResponse createTransaction(TransactionDtoRequest transactionDto){
        User user = userRepository.findById(transactionDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        Bill bill = billRepository.findById(transactionDto.getBillId())
                .orElseThrow(() -> new RuntimeException("Bill Not Found"));

        Transaction transaction = transactionMapper.toTransaction(transactionDto);

        if(transaction.isType()){
            bill.addAmount(transaction.getAmount());
        }
        else{
            bill.subtractAmount(transaction.getAmount());
        }

        transaction.setUser(user);
        transaction.setBill(bill);

        transactionRepository.save(transaction);

        return transactionMapper.toTransactionDto(transaction);
    }

    @Override
    public TransactionDtoResponse updateTransaction(long transactionId, TransactionDtoRequest transactionDto){
        Transaction existingTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        User user = existingTransaction.getUser();
        Bill bill = existingTransaction.getBill();

        double oldAmount = existingTransaction.getAmount();
        double newAmount = transactionDto.getAmount();
        boolean oldType = existingTransaction.isType();
        boolean newType = transactionDto.isType();

        if (!newType && newAmount > bill.getBalance()) {
            throw new RuntimeException("Insufficient funds in the bill for the new transaction amount.");
        }

        existingTransaction.setName(transactionDto.getName());
        existingTransaction.setDescription(transactionDto.getDescription());
        existingTransaction.setType(transactionDto.isType());
        existingTransaction.setAmount(newAmount);
        existingTransaction.setDate(transactionDto.getDate());

        if (!oldType) { // Если старый тип false
            bill.addAmount(oldAmount);
        } else { // Если старый тип true
            bill.subtractAmount(oldAmount);
        }
        if (newType) { // Если новый тип true
            bill.addAmount(existingTransaction.getAmount());
        } else { // Если новый тип false
            bill.subtractAmount(existingTransaction.getAmount());
        }

        userRepository.save(user);
        billRepository.save(bill);
        transactionRepository.save(existingTransaction);
        return transactionMapper.toTransactionDto(existingTransaction);
    }

    @Override
    public void deleteTransaction(long transactionId){
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException(
                        String.format(TRANSACTION_WITH_ID_NOT_FOUND, transactionId)));
        Bill bill = billRepository.findById(transaction.getBill().getId())
                .orElseThrow(() -> new RuntimeException(
                        "Bill not found with id " + transaction.getBill().getId()));

        bill.subtractAmount(transaction.getAmount());

        transactionRepository.delete(transaction);
    }
}
