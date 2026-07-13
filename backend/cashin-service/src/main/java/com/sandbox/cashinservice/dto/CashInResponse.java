package com.sandbox.cashinservice.dto;

public class CashInResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public CashInResponse() {
    }

    public CashInResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> CashInResponse<T> success(T data) {
        return new CashInResponse<>(true, "Success", data);
    }

    public static <T> CashInResponse<T> error(String message) {
        return new CashInResponse<>(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
