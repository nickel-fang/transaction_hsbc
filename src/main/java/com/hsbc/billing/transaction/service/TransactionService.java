package com.hsbc.billing.transaction.service;

import com.hsbc.billing.transaction.dto.TransactionRequest;
import com.hsbc.billing.transaction.dto.TransactionResponse;

import java.util.List;

/**
 * @author Nickel Fang 2025/4/24
 */
public interface TransactionService {

    TransactionResponse createTransaction(TransactionRequest transactionRequest);

    TransactionResponse updateTransaction(Long id, TransactionRequest transactionRequest);

    void deleteTransaction(Long id);

    TransactionResponse getTransaction(Long id);

    List<TransactionResponse> getAllTransactions(Integer page, Integer size);
}
