package com.hsbc.billing.transaction.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsbc.billing.transaction.dto.TransactionRequest;
import com.hsbc.billing.transaction.dto.TransactionResponse;
import com.hsbc.billing.transaction.exception.TransactionDuplicatedException;
import com.hsbc.billing.transaction.exception.TransactionNotFoundException;
import com.hsbc.billing.transaction.model.Transaction;
import com.hsbc.billing.transaction.model.TransactionStatus;
import com.hsbc.billing.transaction.model.TransactionType;
import com.hsbc.billing.transaction.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nickel Fang 2025/4/24
 */
@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

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

    }

    @Test
    public void createTransaction_returns201() throws Exception {
        final Transaction transaction = Transaction.fromDTO(transactionRequest);
        transaction.setId(1l);
        final TransactionResponse transactionResponse = Transaction.toDTO(transaction);
        when(transactionService.createTransaction(any(TransactionRequest.class))).thenReturn(transactionResponse);

        final String content = mockMvc.perform(post("/api/v2/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final TransactionResponse created = objectMapper.readValue(content, TransactionResponse.class);
        assertEquals(1l, created.getId());
        assertNotNull(created.getTransactionTime());
    }

    @Test
    public void createTransaction_returns400() throws Exception {
        transactionRequest.setCurrency("Chinese yuan");

        mockMvc.perform(post("/api/v2/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createTransaction_returns409() throws Exception {
        final Transaction transaction = Transaction.fromDTO(transactionRequest);
        transaction.setId(1l);
        when(transactionService.createTransaction(any(TransactionRequest.class))).thenThrow(new TransactionDuplicatedException(transactionRequest));

        mockMvc.perform(post("/api/v2/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    public void updateTransaction_returns200() throws Exception {
        transactionRequest.setAmount(BigDecimal.valueOf(99.99));
        final Transaction transaction = Transaction.fromDTO(transactionRequest);
        transaction.setId(1l);
        final TransactionResponse transactionResponse = Transaction.toDTO(transaction);
        when(transactionService.updateTransaction(eq(1l), any(TransactionRequest.class))).thenReturn(transactionResponse);

        final String content = mockMvc.perform(put("/api/v2/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final TransactionResponse updated = objectMapper.readValue(content, TransactionResponse.class);
        assertEquals(1l, updated.getId());
        assertEquals(BigDecimal.valueOf(99.99), updated.getAmount());
    }

    @Test
    public void updateTransaction_returns404() throws Exception {
        when(transactionService.updateTransaction(eq(1l), any(TransactionRequest.class))).thenThrow(new TransactionNotFoundException(1l));
        mockMvc.perform(put("/api/v2/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteTransaction_returns204() throws Exception {
        doNothing().when(transactionService).deleteTransaction(1l);
        mockMvc.perform(delete("/api/v2/transactions/1")).andExpect(status().isNoContent());
    }

    @Test
    public void deleteTransaction_returns404() throws Exception {
        doThrow(new TransactionNotFoundException(1l)).when(transactionService).deleteTransaction(1l);
        mockMvc.perform(delete("/api/v2/transactions/1")).andExpect(status().isNotFound());
    }

    @Test
    public void getTransaction_returns200() throws Exception {
        final Transaction transaction = Transaction.fromDTO(transactionRequest);
        transaction.setId(1l);
        final TransactionResponse transactionResponse = Transaction.toDTO(transaction);
        when(transactionService.getTransaction(1l)).thenReturn(transactionResponse);

        final String content = mockMvc.perform(get("/api/v2/transactions/1"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        final TransactionResponse get = objectMapper.readValue(content, TransactionResponse.class);
        assertEquals(1l, get.getId());
        assertNotNull(get.getTransactionTime());
    }

    @Test
    public void getTransaction_returns404() throws Exception {
        when(transactionService.getTransaction(eq(1l))).thenThrow(new TransactionNotFoundException(1l));
        mockMvc.perform(get("/api/v2/transactions/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getAllTransactions_returns200() throws Exception {
        List<TransactionResponse> responses = new ArrayList<>();
        final Transaction transaction = Transaction.fromDTO(transactionRequest);
        transaction.setId(1l);
        final TransactionResponse transactionResponse = Transaction.toDTO(transaction);
        responses.add(transactionResponse);
        when(transactionService.getAllTransactions(any(Integer.class), any(Integer.class))).thenReturn(responses);

        final String content = mockMvc.perform(get("/api/v2/transactions"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List transactionResponses = objectMapper.readValue(content, List.class);
        assertEquals(1l, transactionResponses.size());
    }
}
