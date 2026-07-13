package com.sandbox.qrisservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class GenerateRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum amount is 1,000 IDR")
    private BigDecimal amount;

    private String customerReference;

    private String transactionType = "PAYMENT";

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

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
}
