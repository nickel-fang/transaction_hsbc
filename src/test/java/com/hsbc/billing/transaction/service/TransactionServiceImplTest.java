package com.hsbc.billing.transaction.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.hsbc.billing.transaction.dto.TransactionRequest;
import com.hsbc.billing.transaction.dto.TransactionResponse;
import com.hsbc.billing.transaction.exception.TransactionDuplicatedException;
import com.hsbc.billing.transaction.exception.TransactionNotFoundException;
import com.hsbc.billing.transaction.model.TransactionStatus;
import com.hsbc.billing.transaction.model.TransactionType;
import com.hsbc.billing.transaction.repository.TransactionRepository;
import com.hsbc.billing.transaction.service.impl.TransactionServiceImpl;
import com.hsbc.billing.transaction.util.SnowflakeIdGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Nickel Fang 2025/4/24
 */
public class TransactionServiceImplTest {

    private TransactionService transactionService;

    private CaffeineCacheManager cacheManager;

    private TransactionRequest transactionRequest;

    @BeforeEach
    public void setUp() {
        transactionRequest = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(88.88))
                .beneficiaryName("Nickel Fang")
                .channel("WeChat")
                .currency("CNY")
                .type(TransactionType.TRANSFER)
                .senderAccount("1111111111111111")
                .receiverAccount("2222222222222222")
                .status(TransactionStatus.PENDING)
                .deviceFingerprint("fingerprint")
                .ipAddress("192.168.1.1")
                .build();

        cacheManager = new CaffeineCacheManager("duplicatedTransactions");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .initialCapacity(1000)
                .maximumSize(100_000)
                .recordStats());

        transactionService = new TransactionServiceImpl(new SnowflakeIdGenerator(1l, 1l), new TransactionRepository(), cacheManager);
    }

    @Test
    public void createTransaction_ok() throws Exception {
        final TransactionResponse transaction = transactionService.createTransaction(transactionRequest);
        assertNotNull(transaction);
        assertNotNull(transaction.getId());
        assertEquals(transactionRequest.getAmount(), transaction.getAmount());
    }

    @Test
    public void createTransaction_duplicated() throws Exception {
        final TransactionResponse transaction = transactionService.createTransaction(transactionRequest);
        Assertions.assertThrows(TransactionDuplicatedException.class, () -> transactionService.createTransaction(transactionRequest));
    }

    @Test
    public void createTransaction_notDuplicated() throws Exception {
        transactionService.createTransaction(transactionRequest);
        // Thread.sleep(61000);
        cacheManager.getCache("duplicatedTransactions").clear();
        transactionService.createTransaction(transactionRequest);
    }

    @Test
    public void getTransaction_ok() throws Exception {
        final TransactionResponse transaction = transactionService.createTransaction(transactionRequest);
        final TransactionResponse found = transactionService.getTransaction(transaction.getId());
        assertNotNull(found);
        assertNotNull(found.getId());
        assertEquals(transactionRequest.getAmount(), found.getAmount());
    }

    @Test
    public void getTransaction_notFound() throws Exception {
        assertThrows(TransactionNotFoundException.class, () -> transactionService.getTransaction(0L));
    }

    @Test
    public void getTransactions_ok() throws Exception {
        transactionService.createTransaction(transactionRequest);
        transactionRequest.setSenderAccount("1234567898765432");
        transactionService.createTransaction(transactionRequest);
        final List<TransactionResponse> transactions = transactionService.getAllTransactions(1, 10);
        assertEquals(2, transactions.size());
    }

    @Test
    public void updateTransaction_ok() throws Exception {
        final TransactionResponse transaction = transactionService.createTransaction(transactionRequest);
        transactionRequest.setAmount(BigDecimal.valueOf(99.99));
        final TransactionResponse updated = transactionService.updateTransaction(transaction.getId(), transactionRequest);
        assertNotNull(updated);
        assertEquals(transactionRequest.getAmount(), updated.getAmount());
    }

    @Test
    public void updateTransaction_notFound() throws Exception {
        assertThrows(TransactionNotFoundException.class, () -> transactionService.updateTransaction(0L, transactionRequest));
    }

    @Test
    public void deleteTransaction_ok() throws Exception {
        final TransactionResponse transaction = transactionService.createTransaction(transactionRequest);
        transactionService.deleteTransaction(transaction.getId());
        assertThrows(TransactionNotFoundException.class, () -> transactionService.getTransaction(transaction.getId()));
    }

    @Test
    public void deleteTransaction_notFound() throws Exception {
        assertThrows(TransactionNotFoundException.class, () -> transactionService.deleteTransaction(0L));
    }
}
