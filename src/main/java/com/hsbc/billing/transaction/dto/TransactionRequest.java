package com.hsbc.billing.transaction.dto;

import com.hsbc.billing.transaction.model.TransactionStatus;
import com.hsbc.billing.transaction.model.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Nickel Fang 2025/4/24
 */
@Data
@Builder
public class TransactionRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency must be 3-letter ISO code")
    private String currency;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @NotBlank(message = "Sender account is required")
    private String senderAccount;

    @NotBlank(message = "Receiver account is required")
    private String receiverAccount;

    private String beneficiaryName;

    @NotBlank(message = "Transaction channel is required")
    private String channel;

    @NotNull(message = "Transaction status is required")
    private TransactionStatus status = TransactionStatus.PENDING;

    private String description;

    private String ipAddress;
    private String deviceFingerprint;

    public String getFeatureInfo() {
        return String.join("_", senderAccount, receiverAccount, String.valueOf(amount), currency);
    }

    public String toSimpleString() {
        return "TransactionRequest{" +
                "senderAccount='" + senderAccount + '\'' +
                ", receiverAccount='" + receiverAccount + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                '}';
    }
}
