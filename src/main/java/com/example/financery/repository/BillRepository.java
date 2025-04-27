package com.example.financery.repository;

import com.example.financery.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {

    @Query(value = "SELECT * FROM bill_table WHERE user_id = ?1", nativeQuery = true)
    List<Bill> findByUser(long user_id);
}
