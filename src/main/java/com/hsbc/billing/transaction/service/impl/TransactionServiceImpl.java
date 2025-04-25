package com.hsbc.billing.transaction.service.impl;

import com.hsbc.billing.transaction.dto.TransactionRequest;
import com.hsbc.billing.transaction.dto.TransactionResponse;
import com.hsbc.billing.transaction.exception.TransactionDuplicatedException;
import com.hsbc.billing.transaction.exception.TransactionNotFoundException;
import com.hsbc.billing.transaction.model.Transaction;
import com.hsbc.billing.transaction.repository.TransactionRepository;
import com.hsbc.billing.transaction.service.TransactionService;
import com.hsbc.billing.transaction.util.SnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Nickel Fang 2025/4/24
 */
@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private SnowflakeIdGenerator idGenerator;

    private TransactionRepository repo;

    private CacheManager cacheManager;

    public TransactionServiceImpl(@Autowired SnowflakeIdGenerator idGenerator, @Autowired TransactionRepository repo,
            @Autowired @Qualifier("cache_duplicatedTransactions") CacheManager cacheManager) {
        this.idGenerator = idGenerator;
        this.repo = repo;
        this.cacheManager = cacheManager;
    }

    @Override
    @CachePut(value = "transactions", key = "#result.id")
    public TransactionResponse createTransaction(TransactionRequest transactionRequest) {
        duplicateTransactionCheck(transactionRequest);
        final Transaction transaction = Transaction.fromDTO(transactionRequest);
        transaction.setId(idGenerator.nextId());
        final Transaction saved = repo.save(transaction);
        log.info("Saved transaction: {}", transaction);
        return Transaction.toDTO(saved);
    }

    // two transaction requests with the same feature info in 1 minute will be considered as duplicated
    private void duplicateTransactionCheck(TransactionRequest transactionRequest) {
        final Cache cache = cacheManager.getCache("duplicatedTransactions");
        final String found = cache.get(transactionRequest.getFeatureInfo(), String.class);
        if (!StringUtils.isEmpty(found)) {
            log.warn("Duplicated transaction request: {}", transactionRequest.toSimpleString());
            throw new TransactionDuplicatedException(transactionRequest);
        }
        cache.put(transactionRequest.getFeatureInfo(), "EXIST");
    }

    @Override
    @CachePut(value = "transactions", key = "#id")
    public TransactionResponse updateTransaction(Long id, TransactionRequest transactionRequest) {
        if (!repo.existsById(id)) {
            log.warn("Transaction with id {} not found with transaction update request: {}", id, transactionRequest.toSimpleString());
            throw new TransactionNotFoundException(id);
        }
        final Transaction transaction = Transaction.fromDTO(transactionRequest);
        transaction.setId(id);
        repo.update(transaction);
        log.info("Updated transaction: {}", transaction);
        return Transaction.toDTO(transaction);
    }

    @Override
    @CacheEvict(value = "transactions", key = "#id")
    public void deleteTransaction(Long id) {
        if (!repo.existsById(id)) {
            log.warn("Transaction with id {} not found", id);
            throw new TransactionNotFoundException(id);
        }
        repo.delete(id);
        log.info("Deleted transaction: {}", id);
    }

    @Override
    @Cacheable(value = "transactions", key = "#id")
    public TransactionResponse getTransaction(Long id) {
        final Transaction transaction = repo.findById(id);
        if (transaction == null) {
            log.warn("Transaction with id {} not found", id);
            throw new TransactionNotFoundException(id);
        }
        return Transaction.toDTO(transaction);
    }

    @Override
    public List<TransactionResponse> getAllTransactions(Integer page, Integer size) {
        return repo.findAll(page - 1, size).stream().map(Transaction::toDTO).collect(Collectors.toList());
    }
}
