package com.sandbox.cashinservice.controller;

import com.sandbox.cashinservice.dto.TopUpTransactionResponse;
import com.sandbox.cashinservice.dto.VirtualAccountResponse;
import com.sandbox.cashinservice.service.CashInService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cashin")
public class CashInController {

    private final CashInService cashInService;

    public CashInController(CashInService cashInService) {
        this.cashInService = cashInService;
    }

    @GetMapping("/va")
    public ResponseEntity<VirtualAccountResponse> getVirtualAccounts(
            @RequestHeader("X-User-Id") Long userId) {
        VirtualAccountResponse response = cashInService.getVirtualAccounts(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions")
    public ResponseEntity<TopUpTransactionResponse> getTransactions(
            @RequestHeader("X-User-Id") Long userId) {
        TopUpTransactionResponse response = cashInService.getTopUpTransactions(userId);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<VirtualAccountResponse> handleRuntimeException(RuntimeException ex) {
        VirtualAccountResponse response = new VirtualAccountResponse(
            "ERROR", 
            ex.getMessage(), 
            null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
