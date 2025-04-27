package com.example.financery.repository;

import com.example.financery.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query(value = "SELECT * FROM transaction_table WHERE user_id = ?1", nativeQuery = true)
    List<Transaction> findByUser(long userId);

    @Query(value = "SELECT * FROM transaction_table WHERE bill_id = ?1", nativeQuery = true)
    List<Transaction> findByBill(long billId);
}
