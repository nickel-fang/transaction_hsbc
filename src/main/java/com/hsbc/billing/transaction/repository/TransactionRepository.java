package com.hsbc.billing.transaction.repository;

import com.hsbc.billing.transaction.model.Transaction;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Nickel Fang 2025/4/24
 */
@Repository
public class TransactionRepository {

    private final ConcurrentMap<Long, Transaction> store = new ConcurrentHashMap<>();

    public Transaction save(Transaction tx) {
        store.put(tx.getId(), tx);
        return tx;
    }

    public Transaction update(Transaction tx) {
        store.put(tx.getId(), tx);
        return tx;
    }

    public Transaction findById(Long id) {
        return store.get(id);
    }

    public List<Transaction> findAll(int page, int size) {
        final ArrayList<Transaction> transactions = new ArrayList<>(store.values());
        final int from = Math.min(page * size, transactions.size());
        final int to = Math.min(from + size, transactions.size());
        return transactions.subList(from, to);
    }

    public void delete(Long id) {
        store.remove(id);
    }

    public boolean existsById(Long id) {
        return store.containsKey(id);
    }
}
