package com.sandbox.cashinservice.service;

import com.sandbox.cashinservice.dto.TopUpTransactionResponse;
import com.sandbox.cashinservice.dto.VirtualAccountResponse;
import com.sandbox.cashinservice.entity.TopUpTransaction;
import com.sandbox.cashinservice.entity.VirtualAccount;
import com.sandbox.cashinservice.repository.TopUpTransactionRepository;
import com.sandbox.cashinservice.repository.VirtualAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CashInService {
    private static final Logger log = LoggerFactory.getLogger(CashInService.class);

    private final VirtualAccountRepository virtualAccountRepository;
    private final TopUpTransactionRepository topUpTransactionRepository;

    public CashInService(VirtualAccountRepository virtualAccountRepository,
                        TopUpTransactionRepository topUpTransactionRepository) {
        this.virtualAccountRepository = virtualAccountRepository;
        this.topUpTransactionRepository = topUpTransactionRepository;
    }

    public VirtualAccountResponse getVirtualAccounts(Long userId) {
        log.info("Fetching virtual accounts for user: {}", userId);

        List<VirtualAccount> virtualAccounts = virtualAccountRepository.findByUserId(userId);

        List<VirtualAccountResponse.VirtualAccountData> accountDataList = virtualAccounts.stream()
            .map(va -> new VirtualAccountResponse.VirtualAccountData(
                va.getId(),
                va.getUserId(),
                va.getBankName(),
                va.getAccountNumber(),
                va.getAccountName(),
                va.getIsActive(),
                va.getCreatedAt()
            ))
            .collect(Collectors.toList());

        return new VirtualAccountResponse("SUCCESS", "Virtual accounts retrieved successfully", accountDataList);
    }

    public TopUpTransactionResponse getTopUpTransactions(Long userId) {
        log.info("Fetching top-up transactions for user: {}", userId);

        List<TopUpTransaction> transactions = topUpTransactionRepository
            .findByUserIdOrderByTransactionDateDesc(userId);

        List<TopUpTransactionResponse.TransactionData> transactionDataList = transactions.stream()
            .map(t -> new TopUpTransactionResponse.TransactionData(
                t.getId(),
                t.getUserId(),
                t.getVaId(),
                t.getAmount(),
                t.getReference(),
                t.getStatus(),
                t.getTransactionDate()
            ))
            .collect(Collectors.toList());

        return new TopUpTransactionResponse("SUCCESS", "Transactions retrieved successfully", transactionDataList);
    }
}
