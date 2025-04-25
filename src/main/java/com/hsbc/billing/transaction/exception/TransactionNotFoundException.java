package com.hsbc.billing.transaction.exception;

/**
 * @author Nickel Fang 2025/4/24
 */
public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(Long id) {
        super("Transaction not found with id: " + id);
    }
}
