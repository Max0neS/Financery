package com.example.financery.controller;

import com.example.financery.dto.BillDtoRequest;
import com.example.financery.dto.BillDtoResponse;
import com.example.financery.model.Bill;
import com.example.financery.repository.BillRepository;
import com.example.financery.service.BillService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bills")
@AllArgsConstructor
public class BillController {

    private final BillService billService;
    private final BillRepository billRepository;

    @GetMapping("/get-all-bills")
    public List<BillDtoResponse> getAllBills() {
        return billService.getAllBills();
    }

    @GetMapping("/get-all-user-bills/{userId}")
    public List<BillDtoResponse> getAllUserBills(@PathVariable long userId) {
        return billService.getBillsByUserId(userId);
    }

    @GetMapping("/get-bill-by-id/{billId}")
    public ResponseEntity<BillDtoResponse> getBillById(@PathVariable long billId) {
        return ResponseEntity.ok(billService.getBillById(billId));
    }

    @PostMapping("/create")
    public BillDtoResponse createBill(@RequestBody BillDtoRequest billDto) {
        BillDtoResponse createBill = billService.createBill(billDto);
        return createBill;
    }

    @PutMapping("/update-by-id/{billId}")
    public BillDtoResponse updateBillById(@PathVariable long billId, @RequestBody BillDtoRequest billDto) {
        return billService.updateBill(billId, billDto);
    }

    @DeleteMapping("/delete-by-id/{billId}")
    public void deleteBillById(@PathVariable long billId) {
        billService.deleteBill(billId);
    }
}
