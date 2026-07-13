package com.sandbox.cashinservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class TopUpTransactionResponse {
    private String status;
    private String message;
    private List<TransactionData> data;

    public TopUpTransactionResponse() {
    }

    public TopUpTransactionResponse(String status, String message, List<TransactionData> data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<TransactionData> getData() {
        return data;
    }

    public void setData(List<TransactionData> data) {
        this.data = data;
    }

    public static class TransactionData {
        private Long id;
        private Long userId;
        private Long vaId;
        private BigDecimal amount;
        private String reference;
        private String status;
        private LocalDateTime transactionDate;

        public TransactionData() {
        }

        public TransactionData(Long id, Long userId, Long vaId, BigDecimal amount,
                              String reference, String status, LocalDateTime transactionDate) {
            this.id = id;
            this.userId = userId;
            this.vaId = vaId;
            this.amount = amount;
            this.reference = reference;
            this.status = status;
            this.transactionDate = transactionDate;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getVaId() {
            return vaId;
        }

        public void setVaId(Long vaId) {
            this.vaId = vaId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getReference() {
            return reference;
        }

        public void setReference(String reference) {
            this.reference = reference;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getTransactionDate() {
            return transactionDate;
        }

        public void setTransactionDate(LocalDateTime transactionDate) {
            this.transactionDate = transactionDate;
        }
    }
}
