package com.sandbox.qrisservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

public class ActivateRequest {
    @NotBlank(message = "Merchant name is required")
    private String merchantName;

    @Pattern(regexp = "^\\d{15}$", message = "NMID must be 15 digits")
    private String nmid;

    @Pattern(regexp = "^\\+62\\d{9,12}$", message = "Phone number must start with +62 and contain 10-13 digits")
    private String phoneNumber;

    private BigDecimal dailyLimit;

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

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }
}
