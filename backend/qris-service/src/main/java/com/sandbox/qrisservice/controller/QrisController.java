package com.sandbox.qrisservice.controller;

import com.sandbox.qrisservice.dto.ActivateRequest;
import com.sandbox.qrisservice.dto.GenerateRequest;
import com.sandbox.qrisservice.dto.QrisResponse;
import com.sandbox.qrisservice.service.QrisService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/qris")
public class QrisController {

    private final QrisService qrisService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${user.service.url:http://user-service-prod:8081}")
    private String userServiceUrl;

    public QrisController(QrisService qrisService) {
        this.qrisService = qrisService;
    }

    @PostMapping("/activate")
    public ResponseEntity<QrisResponse> activate(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ActivateRequest request) {
        QrisResponse response = qrisService.activate(userId, request);
        
        // Grant QRIS privilege after successful activation
        try {
            restTemplate.postForEntity(
                userServiceUrl + "/api/privileges/grant?userId=" + userId + "&feature=QRIS",
                null, String.class);
        } catch (Exception e) {
            // Log but don't fail - activation already succeeded
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/generate")
    public ResponseEntity<QrisResponse> generateQris(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody GenerateRequest request) {
        QrisResponse response = qrisService.generateQris(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/transactions")
    public ResponseEntity<QrisResponse> getTransactions(@RequestHeader("X-User-Id") Long userId) {
        QrisResponse response = qrisService.getTransactions(userId);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<QrisResponse> handleRuntimeException(RuntimeException ex) {
        QrisResponse response = new QrisResponse("ERROR", ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
