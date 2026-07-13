package com.sandbox.qrisservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class QrisResponse {
    private String status;
    private String message;
    private Object data;
    private Boolean dummy;

    public QrisResponse() {
    }

    public QrisResponse(String status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public QrisResponse(String status, String message, Object data, Boolean dummy) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.dummy = dummy;
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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Boolean getDummy() {
        return dummy;
    }

    public void setDummy(Boolean dummy) {
        this.dummy = dummy;
    }

    public static class MerchantData {
        private Long id;
        private Long userId;
        private String merchantName;
        private String nmid;
        private String phoneNumber;
        private Boolean isActive;
        private BigDecimal dailyLimit;
        private LocalDateTime createdAt;

        public MerchantData() {
        }

        public MerchantData(Long id, Long userId, String merchantName, String nmid, 
                           String phoneNumber, Boolean isActive, BigDecimal dailyLimit, 
                           LocalDateTime createdAt) {
            this.id = id;
            this.userId = userId;
            this.merchantName = merchantName;
            this.nmid = nmid;
            this.phoneNumber = phoneNumber;
            this.isActive = isActive;
            this.dailyLimit = dailyLimit;
            this.createdAt = createdAt;
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

        public String getMerchantName() {
            return merchantName;
        }

        public void setMerchantName(String merchantName) {
            this.merchantName = merchantName;
        }

        public String getNmid() {
            return nmid;
        }

        public void setNmid(String nmid) {
            this.nmid = nmid;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public Boolean getIsActive() {
            return isActive;
        }

        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
        }

        public BigDecimal getDailyLimit() {
            return dailyLimit;
        }

        public void setDailyLimit(BigDecimal dailyLimit) {
            this.dailyLimit = dailyLimit;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }

    public static class TransactionData {
        private Long id;
        private String transactionId;
        private BigDecimal amount;
        private String customerReference;
        private String status;
        private String qrisCode;
        private String transactionType;
        private LocalDateTime createdAt;

        public TransactionData() {
        }

        public TransactionData(Long id, String transactionId, BigDecimal amount, 
                              String customerReference, String status, String qrisCode, 
                              String transactionType, LocalDateTime createdAt) {
            this.id = id;
            this.transactionId = transactionId;
            this.amount = amount;
            this.customerReference = customerReference;
            this.status = status;
            this.qrisCode = qrisCode;
            this.transactionType = transactionType;
            this.createdAt = createdAt;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getCustomerReference() {
            return customerReference;
        }

        public void setCustomerReference(String customerReference) {
            this.customerReference = customerReference;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getQrisCode() {
            return qrisCode;
        }

        public void setQrisCode(String qrisCode) {
            this.qrisCode = qrisCode;
        }

        public String getTransactionType() {
            return transactionType;
        }

        public void setTransactionType(String transactionType) {
            this.transactionType = transactionType;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}
