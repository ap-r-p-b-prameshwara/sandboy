package com.sandbox.cashinservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public class VirtualAccountResponse {
    private String status;
    private String message;
    private List<VirtualAccountData> data;

    public VirtualAccountResponse() {
    }

    public VirtualAccountResponse(String status, String message, List<VirtualAccountData> data) {
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

    public List<VirtualAccountData> getData() {
        return data;
    }

    public void setData(List<VirtualAccountData> data) {
        this.data = data;
    }

    public static class VirtualAccountData {
        private Long id;
        private Long userId;
        private String bankName;
        private String accountNumber;
        private String accountName;
        private Boolean isActive;
        private LocalDateTime createdAt;

        public VirtualAccountData() {
        }

        public VirtualAccountData(Long id, Long userId, String bankName, String accountNumber,
                                  String accountName, Boolean isActive, LocalDateTime createdAt) {
            this.id = id;
            this.userId = userId;
            this.bankName = bankName;
            this.accountNumber = accountNumber;
            this.accountName = accountName;
            this.isActive = isActive;
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

        public String getBankName() {
            return bankName;
        }

        public void setBankName(String bankName) {
            this.bankName = bankName;
        }

        public String getAccountNumber() {
            return accountNumber;
        }

        public void setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
        }

        public String getAccountName() {
            return accountName;
        }

        public void setAccountName(String accountName) {
            this.accountName = accountName;
        }

        public Boolean getIsActive() {
            return isActive;
        }

        public void setIsActive(Boolean isActive) {
            this.isActive = isActive;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }
    }
}
