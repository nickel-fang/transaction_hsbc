package com.hsbc.billing.transaction.stress;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsbc.billing.transaction.dto.TransactionRequest;
import com.hsbc.billing.transaction.dto.TransactionResponse;
import com.hsbc.billing.transaction.model.TransactionStatus;
import com.hsbc.billing.transaction.model.TransactionType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Nickel Fang 2025/4/24
 *
 * start the transaction-api service before the stress test, so add @Disabled for command "mvn test"
 */
@SpringBootTest
@Disabled
public class TransactionApiStressTest {

    private final String baseUrl = "http://localhost:8080/api/v2/transactions";
    private final Random random = new Random();

    private final RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private ObjectMapper objectMapper;
    private final int testCount = 10_000;
    private final int threadCount = 10;
    private final ConcurrentLinkedQueue<Long> transactionIds = new ConcurrentLinkedQueue<>();
    private List<Long> transactionList;

    @Test
    public void runStressTest() throws Exception {
        stressTestCreateTransaction();
        transactionList = new ArrayList<>(transactionIds);
        stressTestGetAllTransactions();
        stressTestGetTransaction();
        stressTestUpdateTransaction();
        stressTestDeleteTransaction();
    }

    private void stressTestCreateTransaction() throws Exception {
        run("CREATE_TRANSACTION");
    }

    private void stressTestGetAllTransactions() throws Exception {
        run("GET_ALL_TRANSACTIONS");
    }

    private void stressTestGetTransaction() throws Exception {
        run("GET_TRANSACTION");
    }

    private void stressTestUpdateTransaction() throws Exception {
        run("UPDATE_TRANSACTION");
    }

    private void stressTestDeleteTransaction() throws Exception {
        run("DELETE_TRANSACTION");
    }


    private void run(String method) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        ConcurrentLinkedQueue<Long> latencies = new ConcurrentLinkedQueue<>();
        AtomicInteger errorCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(testCount);

        for (int i = 0; i < testCount; i++) {
            executorService.submit(() -> {
                try {
                    Instant start = Instant.now();
                    switch (method) {
                        case "CREATE_TRANSACTION":
                            ResponseEntity<TransactionResponse> response = restTemplate.postForEntity(baseUrl, generateTransactionRequest(),
                                    TransactionResponse.class);
                            transactionIds.add(response.getBody().getId());
                            break;
                        case "GET_ALL_TRANSACTIONS":
                            int page = random.nextInt(transactionIds.size() / 10) + 1;
                            restTemplate.getForEntity(baseUrl + "?page=" + page + "&size=10", (Class<List<TransactionResponse>>) (Class<?>) List.class);
                            break;
                        case "GET_TRANSACTION":
                            restTemplate.getForEntity(baseUrl + "/" + getTransactionId(), TransactionResponse.class);
                            break;
                        case "UPDATE_TRANSACTION":
                            restTemplate.exchange(baseUrl + "/" + getTransactionId(), HttpMethod.PUT, new HttpEntity<>(generateTransactionRequest()),
                                    TransactionResponse.class);
                            break;
                        case "DELETE_TRANSACTION":
                            restTemplate.delete(baseUrl + "/" + transactionIds.remove());
                            break;
                        default:
                            throw new UnsupportedOperationException("Method not supported: " + method);
                    }
                    Instant end = Instant.now();

                    latencies.add(Duration.between(start, end).toMillis());
                } catch (Exception e) {
                    System.out.println(e);
                    errorCount.getAndIncrement();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(1, TimeUnit.MINUTES);
        reportMetrics(method, latencies, errorCount.get());
    }

    private Long getTransactionId() {
        if (!CollectionUtils.isEmpty(transactionList)) {
            return transactionList.get(random.nextInt(transactionList.size() - 1));
        }
        return 0l;
    }

    private void reportMetrics(String method, Collection<Long> latencies, int errorCount) {
        long totalLatency = latencies.stream().mapToLong(Long::longValue).sum();
        long maxLatency = latencies.stream().mapToLong(Long::longValue).max().orElse(0);
        long minLatency = latencies.stream().mapToLong(Long::longValue).min().orElse(0);
        double avgLatency = latencies.isEmpty() ? 0 : (double) totalLatency / latencies.size();

        System.out.println("--- Stress Test Metrics of " + method + "---");
        System.out.println("Requests: " + testCount);
        System.out.println("errors: " + errorCount);
        System.out.println("Throughput: " + (latencies.size() * 1000.0 / (totalLatency + 1)) + " req/sec");
        System.out.println("Latency (min/avg/max): " + minLatency + "/" + avgLatency + "/" + maxLatency + " ms");
    }

    private TransactionRequest generateTransactionRequest() throws JsonProcessingException {
        final TransactionRequest transactionRequest = TransactionRequest.builder()
                .amount(BigDecimal.valueOf(88.88))
                .beneficiaryName("Nickel Fang")
                .channel("WeChat")
                .currency("CNY")
                .type(TransactionType.TRANSFER)
                .senderAccount(String.valueOf(random.nextInt(Integer.MAX_VALUE)))
                .receiverAccount(String.valueOf(random.nextInt(Integer.MAX_VALUE)))
                .status(TransactionStatus.PENDING)
                .deviceFingerprint("fingerprint")
                .ipAddress("192.168.1.1")
                .build();
        return transactionRequest;
    }
}
