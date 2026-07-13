package com.sandbox.cashinservice.service;

import com.sandbox.cashinservice.dto.TopUpTransactionResponse;
import com.sandbox.cashinservice.dto.VirtualAccountResponse;
import com.sandbox.cashinservice.entity.TopUpTransaction;
import com.sandbox.cashinservice.entity.VirtualAccount;
import com.sandbox.cashinservice.repository.TopUpTransactionRepository;
import com.sandbox.cashinservice.repository.VirtualAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CashInService Unit Tests")
class CashInServiceTest {

    @Mock
    private VirtualAccountRepository virtualAccountRepository;

    @Mock
    private TopUpTransactionRepository topUpTransactionRepository;

    @InjectMocks
    private CashInService cashInService;

    private VirtualAccount testVirtualAccount;
    private TopUpTransaction testTransaction;
    private final Long testUserId = 1L;
    private final Long testVaId = 10L;
    private final Long testTxId = 100L;

    @BeforeEach
    void setUp() {
        testVirtualAccount = new VirtualAccount();
        testVirtualAccount.setId(testVaId);
        testVirtualAccount.setUserId(testUserId);
        testVirtualAccount.setBankName("Test Bank");
        testVirtualAccount.setAccountNumber("1234567890");
        testVirtualAccount.setAccountName("Test Merchant");
        testVirtualAccount.setIsActive(true);
        testVirtualAccount.setCreatedAt(LocalDateTime.now());

        testTransaction = new TopUpTransaction();
        testTransaction.setId(testTxId);
        testTransaction.setUserId(testUserId);
        testTransaction.setVaId(testVaId);
        testTransaction.setAmount(new BigDecimal("100000"));
        testTransaction.setReference("REF-001");
        testTransaction.setStatus("SUCCESS");
        testTransaction.setTransactionDate(LocalDateTime.now());
    }

    @Test
    @DisplayName("getVirtualAccounts - should return list of virtual accounts")
    void getVirtualAccounts_Success() {
        VirtualAccount secondVa = new VirtualAccount();
        secondVa.setId(11L);
        secondVa.setUserId(testUserId);
        secondVa.setBankName("Second Bank");
        secondVa.setAccountNumber("0987654321");
        secondVa.setAccountName("Test Merchant 2");
        secondVa.setIsActive(false);

        when(virtualAccountRepository.findByUserId(testUserId))
            .thenReturn(Arrays.asList(testVirtualAccount, secondVa));

        VirtualAccountResponse response = cashInService.getVirtualAccounts(testUserId);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getData()).hasSize(2);

        VirtualAccountResponse.VirtualAccountData firstVa = response.getData().get(0);
        assertThat(firstVa.getId()).isEqualTo(testVaId);
        assertThat(firstVa.getUserId()).isEqualTo(testUserId);
        assertThat(firstVa.getBankName()).isEqualTo("Test Bank");
        assertThat(firstVa.getAccountNumber()).isEqualTo("1234567890");
        assertThat(firstVa.getAccountName()).isEqualTo("Test Merchant");
        assertThat(firstVa.getIsActive()).isTrue();

        verify(virtualAccountRepository).findByUserId(testUserId);
    }

    @Test
    @DisplayName("getVirtualAccounts - should return empty list when no VAs")
    void getVirtualAccounts_NoAccounts_ReturnsEmptyList() {
        when(virtualAccountRepository.findByUserId(testUserId)).thenReturn(Arrays.asList());

        VirtualAccountResponse response = cashInService.getVirtualAccounts(testUserId);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isEmpty();

        verify(virtualAccountRepository).findByUserId(testUserId);
    }

    @Test
    @DisplayName("getVirtualAccounts - should return inactive VAs as well")
    void getVirtualAccounts_IncludesInactiveAccounts() {
        VirtualAccount inactiveVa = new VirtualAccount();
        inactiveVa.setId(12L);
        inactiveVa.setUserId(testUserId);
        inactiveVa.setBankName("Inactive Bank");
        inactiveVa.setAccountNumber("5555555555");
        inactiveVa.setAccountName("Inactive Merchant");
        inactiveVa.setIsActive(false);

        when(virtualAccountRepository.findByUserId(testUserId))
            .thenReturn(Arrays.asList(testVirtualAccount, inactiveVa));

        VirtualAccountResponse response = cashInService.getVirtualAccounts(testUserId);

        assertThat(response.getData()).hasSize(2);
        assertThat(response.getData().get(1).getIsActive()).isFalse();
    }

    @Test
    @DisplayName("getTopUpTransactions - should return list of transactions")
    void getTopUpTransactions_Success() {
        TopUpTransaction secondTx = new TopUpTransaction();
        secondTx.setId(101L);
        secondTx.setUserId(testUserId);
        secondTx.setVaId(testVaId);
        secondTx.setAmount(new BigDecimal("50000"));
        secondTx.setReference("REF-002");
        secondTx.setStatus("PENDING");
        secondTx.setTransactionDate(LocalDateTime.now().minusHours(1));

        when(topUpTransactionRepository.findByUserIdOrderByTransactionDateDesc(testUserId))
            .thenReturn(Arrays.asList(testTransaction, secondTx));

        TopUpTransactionResponse response = cashInService.getTopUpTransactions(testUserId);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getData()).hasSize(2);

        TopUpTransactionResponse.TransactionData firstTx = response.getData().get(0);
        assertThat(firstTx.getId()).isEqualTo(testTxId);
        assertThat(firstTx.getUserId()).isEqualTo(testUserId);
        assertThat(firstTx.getAmount()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(firstTx.getReference()).isEqualTo("REF-001");
        assertThat(firstTx.getStatus()).isEqualTo("SUCCESS");

        verify(topUpTransactionRepository).findByUserIdOrderByTransactionDateDesc(testUserId);
    }

    @Test
    @DisplayName("getTopUpTransactions - should return empty list when no transactions")
    void getTopUpTransactions_NoTransactions_ReturnsEmptyList() {
        when(topUpTransactionRepository.findByUserIdOrderByTransactionDateDesc(testUserId))
            .thenReturn(Arrays.asList());

        TopUpTransactionResponse response = cashInService.getTopUpTransactions(testUserId);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isEmpty();

        verify(topUpTransactionRepository).findByUserIdOrderByTransactionDateDesc(testUserId);
    }

    @Test
    @DisplayName("getTopUpTransactions - transactions should be ordered by date descending")
    void getTopUpTransactions_OrderedByDateDesc() {
        TopUpTransaction olderTx = new TopUpTransaction();
        olderTx.setId(101L);
        olderTx.setUserId(testUserId);
        olderTx.setVaId(testVaId);
        olderTx.setAmount(new BigDecimal("25000"));
        olderTx.setReference("REF-OLD");
        olderTx.setStatus("SUCCESS");
        olderTx.setTransactionDate(LocalDateTime.now().minusDays(1));

        when(topUpTransactionRepository.findByUserIdOrderByTransactionDateDesc(testUserId))
            .thenReturn(Arrays.asList(testTransaction, olderTx));

        TopUpTransactionResponse response = cashInService.getTopUpTransactions(testUserId);

        assertThat(response.getData()).hasSize(2);
        assertThat(response.getData().get(0).getReference()).isEqualTo("REF-001");
        assertThat(response.getData().get(1).getReference()).isEqualTo("REF-OLD");
    }

    @Test
    @DisplayName("getVirtualAccounts - should handle multiple VAs for same user")
    void getVirtualAccounts_MultipleAccounts_Success() {
        List<VirtualAccount> vaList = Arrays.asList(
            testVirtualAccount,
            createVa(11L, testUserId, "BCA", "111111", "VA BCA"),
            createVa(12L, testUserId, "BNI", "222222", "VA BNI")
        );

        when(virtualAccountRepository.findByUserId(testUserId)).thenReturn(vaList);

        VirtualAccountResponse response = cashInService.getVirtualAccounts(testUserId);

        assertThat(response.getData()).hasSize(3);
        assertThat(response.getData().get(1).getBankName()).isEqualTo("BCA");
        assertThat(response.getData().get(2).getBankName()).isEqualTo("BNI");
    }

    private VirtualAccount createVa(Long id, Long userId, String bank, String accNum, String accName) {
        VirtualAccount va = new VirtualAccount();
        va.setId(id);
        va.setUserId(userId);
        va.setBankName(bank);
        va.setAccountNumber(accNum);
        va.setAccountName(accName);
        va.setIsActive(true);
        va.setCreatedAt(LocalDateTime.now());
        return va;
    }
}
