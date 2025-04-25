package com.hsbc.billing.transaction.dto;

import com.hsbc.billing.transaction.model.TransactionStatus;
import com.hsbc.billing.transaction.model.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author Nickel Fang 2025/4/24
 */
@Data
public class TransactionResponse {
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
}
