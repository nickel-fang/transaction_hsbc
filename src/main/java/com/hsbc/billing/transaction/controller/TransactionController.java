package com.hsbc.billing.transaction.controller;

import com.hsbc.billing.transaction.dto.TransactionRequest;
import com.hsbc.billing.transaction.dto.TransactionResponse;
import com.hsbc.billing.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Nickel Fang 2025/4/24
 */
@RestController
@RequestMapping("/api/v2/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody @Valid TransactionRequest transactionRequest) {
        final TransactionResponse created = transactionService.createTransaction(transactionRequest);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransaction(id));
    }


    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(transactionService.getAllTransactions(page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(@PathVariable Long id, @RequestBody @Valid TransactionRequest transactionRequest) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, transactionRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
