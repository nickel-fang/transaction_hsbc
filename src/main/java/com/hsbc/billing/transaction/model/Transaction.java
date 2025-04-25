package com.hsbc.billing.transaction.model;

import com.hsbc.billing.transaction.dto.TransactionRequest;
import com.hsbc.billing.transaction.dto.TransactionResponse;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author Nickel Fang 2025/4/24
 */
@Data
public class Transaction {

    private Long id; // Snowflake ID

    private BigDecimal amount;
    private String currency;
    private TransactionType type;
    private String senderAccount;
    private String receiverAccount;
    private String beneficiaryName;
    private String channel;
    private TransactionStatus status;
    private String description;
    private LocalDateTime transactionTime;

    private String ipAddress;
    private String deviceFingerprint;

    public static Transaction fromDTO(TransactionRequest transactionRequest) {
        Transaction transaction = new Transaction();
        transaction.setAmount(transactionRequest.getAmount());
        transaction.setCurrency(transactionRequest.getCurrency());
        transaction.setType(transactionRequest.getType());
        transaction.setSenderAccount(transactionRequest.getSenderAccount());
        transaction.setReceiverAccount(transactionRequest.getReceiverAccount());
        transaction.setBeneficiaryName(transactionRequest.getBeneficiaryName());
        transaction.setChannel(transactionRequest.getChannel());
        transaction.setStatus(transactionRequest.getStatus());
        transaction.setDescription(transactionRequest.getDescription());
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setIpAddress(transactionRequest.getIpAddress());
        transaction.setDeviceFingerprint(transactionRequest.getDeviceFingerprint());
        return transaction;
    }

    public static TransactionResponse toDTO(Transaction transaction) {
        TransactionResponse transactionResponse = new TransactionResponse();
        transactionResponse.setId(transaction.getId());
        transactionResponse.setAmount(transaction.getAmount());
        transactionResponse.setCurrency(transaction.getCurrency());
        transactionResponse.setType(transaction.getType());
        transactionResponse.setSenderAccount(transaction.getSenderAccount());
        transactionResponse.setReceiverAccount(transaction.getReceiverAccount());
        transactionResponse.setBeneficiaryName(transaction.getBeneficiaryName());
        transactionResponse.setChannel(transaction.getChannel());
        transactionResponse.setStatus(transaction.getStatus());
        transactionResponse.setDescription(transaction.getDescription());
        transactionResponse.setTransactionTime(transaction.getTransactionTime());
        return transactionResponse;
    }
}
