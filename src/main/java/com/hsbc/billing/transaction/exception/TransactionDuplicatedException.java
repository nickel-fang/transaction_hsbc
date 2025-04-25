package com.hsbc.billing.transaction.exception;

import com.hsbc.billing.transaction.dto.TransactionRequest;

/**
 * @author Nickel Fang 2025/4/24
 */
public class TransactionDuplicatedException extends RuntimeException {

    public TransactionDuplicatedException(TransactionRequest transactionRequest) {
        super("Transaction duplicated with " + transactionRequest.toSimpleString());
    }
}
