package com.sandbox.qrisservice.service;

import com.sandbox.qrisservice.dto.ActivateRequest;
import com.sandbox.qrisservice.dto.GenerateRequest;
import com.sandbox.qrisservice.dto.QrisResponse;
import com.sandbox.qrisservice.entity.QrisMerchant;
import com.sandbox.qrisservice.entity.QrisTransaction;
import com.sandbox.qrisservice.repository.QrisMerchantRepository;
import com.sandbox.qrisservice.repository.QrisTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QrisService {
    private static final Logger log = LoggerFactory.getLogger(QrisService.class);

    private final QrisMerchantRepository merchantRepository;
    private final QrisTransactionRepository transactionRepository;

    @Value("${qris.dummy.enabled:false}")
    private boolean dummyEnabled;

    public QrisService(QrisMerchantRepository merchantRepository, 
                       QrisTransactionRepository transactionRepository) {
        this.merchantRepository = merchantRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public QrisResponse activate(Long userId, ActivateRequest request) {
        log.info("Activating QRIS merchant for user: {}", userId);

        if (merchantRepository.existsByUserId(userId)) {
            throw new RuntimeException("Merchant already activated for user: " + userId);
        }

        if (request.getNmid() != null && merchantRepository.existsByNmid(request.getNmid())) {
            throw new RuntimeException("NMID already registered: " + request.getNmid());
        }

        QrisMerchant merchant = new QrisMerchant();
        merchant.setUserId(userId);
        merchant.setMerchantName(request.getMerchantName());
        merchant.setNmid(request.getNmid());
        merchant.setPhoneNumber(request.getPhoneNumber());
        merchant.setDailyLimit(request.getDailyLimit());
        merchant.setIsActive(true);

        QrisMerchant savedMerchant = merchantRepository.save(merchant);

        log.info("QRIS merchant activated successfully with id: {}", savedMerchant.getId());

        QrisResponse.MerchantData data = new QrisResponse.MerchantData(
            savedMerchant.getId(),
            savedMerchant.getUserId(),
            savedMerchant.getMerchantName(),
            savedMerchant.getNmid(),
            savedMerchant.getPhoneNumber(),
            savedMerchant.getIsActive(),
            savedMerchant.getDailyLimit(),
            savedMerchant.getCreatedAt()
        );

        return new QrisResponse("SUCCESS", "Merchant activated successfully", data, dummyEnabled);
    }

    @Transactional
    public QrisResponse generateQris(Long userId, GenerateRequest request) {
        log.info("Generating QRIS code for user: {}, amount: {}", userId, request.getAmount());

        QrisMerchant merchant = merchantRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Merchant not found for user: " + userId));

        if (!merchant.getIsActive()) {
            throw new RuntimeException("Merchant is not active");
        }

        String transactionId = generateTransactionId();
        String qrisCode = generateQrisCode(merchant, request, transactionId);

        QrisTransaction transaction = new QrisTransaction();
        transaction.setMerchantId(merchant.getId());
        transaction.setTransactionId(transactionId);
        transaction.setAmount(request.getAmount());
        transaction.setCustomerReference(request.getCustomerReference());
        transaction.setStatus("PENDING");
        transaction.setQrisCode(qrisCode);
        transaction.setTransactionType(request.getTransactionType());

        QrisTransaction savedTransaction = transactionRepository.save(transaction);

        log.info("QRIS transaction created with id: {}", savedTransaction.getId());

        QrisResponse.TransactionData data = new QrisResponse.TransactionData(
            savedTransaction.getId(),
            savedTransaction.getTransactionId(),
            savedTransaction.getAmount(),
            savedTransaction.getCustomerReference(),
            savedTransaction.getStatus(),
            savedTransaction.getQrisCode(),
            savedTransaction.getTransactionType(),
            savedTransaction.getCreatedAt()
        );

        return new QrisResponse("SUCCESS", "QRIS code generated successfully", data, dummyEnabled);
    }

    public QrisResponse getTransactions(Long userId) {
        log.info("Fetching transactions for user: {}", userId);

        QrisMerchant merchant = merchantRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Merchant not found for user: " + userId));

        List<QrisTransaction> transactions = transactionRepository
            .findByMerchantIdOrderByCreatedAtDesc(merchant.getId());

        List<QrisResponse.TransactionData> transactionDataList = transactions.stream()
            .map(t -> new QrisResponse.TransactionData(
                t.getId(),
                t.getTransactionId(),
                t.getAmount(),
                t.getCustomerReference(),
                t.getStatus(),
                t.getQrisCode(),
                t.getTransactionType(),
                t.getCreatedAt()
            ))
            .collect(Collectors.toList());

        return new QrisResponse("SUCCESS", "Transactions retrieved successfully", 
                               transactionDataList, dummyEnabled);
    }

    private String generateTransactionId() {
        return "QRIS" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateQrisCode(QrisMerchant merchant, GenerateRequest request, String transactionId) {
        if (dummyEnabled) {
            return "DUMMY_QRIS_CODE_" + transactionId + "_" + request.getAmount();
        }

        StringBuilder qris = new StringBuilder();
        qris.append("000201");
        qris.append("010211");
        qris.append("02").append(String.format("%02d", merchant.getNmid().length())).append(merchant.getNmid());
        qris.append("5204").append("0000");
        qris.append("5303").append("360");
        qris.append("54").append(String.format("%02d", request.getAmount().toString().length())).append(request.getAmount());
        qris.append("5802ID");
        qris.append("59").append(String.format("%02d", merchant.getMerchantName().length())).append(merchant.getMerchantName());
        qris.append("6013").append("JAKARTA PUSAT");
        qris.append("6105").append("10340");
        qris.append("6304");

        return qris.toString();
    }
}
