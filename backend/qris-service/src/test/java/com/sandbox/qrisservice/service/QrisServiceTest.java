package com.sandbox.qrisservice.service;

import com.sandbox.qrisservice.dto.ActivateRequest;
import com.sandbox.qrisservice.dto.GenerateRequest;
import com.sandbox.qrisservice.dto.QrisResponse;
import com.sandbox.qrisservice.entity.QrisMerchant;
import com.sandbox.qrisservice.entity.QrisTransaction;
import com.sandbox.qrisservice.repository.QrisMerchantRepository;
import com.sandbox.qrisservice.repository.QrisTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QrisService Unit Tests")
class QrisServiceTest {

    @Mock
    private QrisMerchantRepository merchantRepository;

    @Mock
    private QrisTransactionRepository transactionRepository;

    @InjectMocks
    private QrisService qrisService;

    private ActivateRequest activateRequest;
    private GenerateRequest generateRequest;
    private QrisMerchant testMerchant;
    private QrisTransaction testTransaction;
    private final Long testUserId = 1L;
    private final Long testMerchantId = 10L;
    private final Long testTransactionId = 100L;

    @BeforeEach
    void setUp() {
        activateRequest = new ActivateRequest();
        activateRequest.setMerchantName("Test Merchant");
        activateRequest.setNmid("ID123456");
        activateRequest.setPhoneNumber("08123456789");
        activateRequest.setDailyLimit(new BigDecimal("10000000"));

        generateRequest = new GenerateRequest();
        generateRequest.setAmount(new BigDecimal("50000"));
        generateRequest.setCustomerReference("INV-001");
        generateRequest.setTransactionType("PAYMENT");

        testMerchant = new QrisMerchant();
        testMerchant.setId(testMerchantId);
        testMerchant.setUserId(testUserId);
        testMerchant.setMerchantName("Test Merchant");
        testMerchant.setNmid("ID123456");
        testMerchant.setPhoneNumber("08123456789");
        testMerchant.setIsActive(true);
        testMerchant.setDailyLimit(new BigDecimal("10000000"));

        testTransaction = new QrisTransaction();
        testTransaction.setId(testTransactionId);
        testTransaction.setMerchantId(testMerchantId);
        testTransaction.setTransactionId("QRIS1234567890");
        testTransaction.setAmount(new BigDecimal("50000"));
        testTransaction.setCustomerReference("INV-001");
        testTransaction.setStatus("PENDING");
        testTransaction.setQrisCode("DUMMY_QRIS_CODE");
        testTransaction.setTransactionType("PAYMENT");
    }

    @Test
    @DisplayName("activate - should activate merchant successfully")
    void activate_Success() {
        when(merchantRepository.existsByUserId(testUserId)).thenReturn(false);
        when(merchantRepository.existsByNmid("ID123456")).thenReturn(false);
        when(merchantRepository.save(any(QrisMerchant.class))).thenReturn(testMerchant);

        QrisResponse response = qrisService.activate(testUserId, activateRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isInstanceOf(QrisResponse.MerchantData.class);

        QrisResponse.MerchantData data = (QrisResponse.MerchantData) response.getData();
        assertThat(data.getId()).isEqualTo(testMerchantId);
        assertThat(data.getUserId()).isEqualTo(testUserId);
        assertThat(data.getMerchantName()).isEqualTo("Test Merchant");
        assertThat(data.getNmid()).isEqualTo("ID123456");

        verify(merchantRepository).existsByUserId(testUserId);
        verify(merchantRepository).existsByNmid("ID123456");
        verify(merchantRepository).save(any(QrisMerchant.class));
    }

    @Test
    @DisplayName("activate - should throw exception when merchant already activated")
    void activate_MerchantAlreadyExists_ThrowsException() {
        when(merchantRepository.existsByUserId(testUserId)).thenReturn(true);

        assertThatThrownBy(() -> qrisService.activate(testUserId, activateRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Merchant already activated");

        verify(merchantRepository).existsByUserId(testUserId);
        verify(merchantRepository, never()).save(any());
    }

    @Test
    @DisplayName("activate - should throw exception when NMID already registered")
    void activate_NmidAlreadyExists_ThrowsException() {
        when(merchantRepository.existsByUserId(testUserId)).thenReturn(false);
        when(merchantRepository.existsByNmid("ID123456")).thenReturn(true);

        assertThatThrownBy(() -> qrisService.activate(testUserId, activateRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("NMID already registered");

        verify(merchantRepository).existsByUserId(testUserId);
        verify(merchantRepository).existsByNmid("ID123456");
        verify(merchantRepository, never()).save(any());
    }

    @Test
    @DisplayName("generateQris - should generate QRIS code successfully")
    void generateQris_Success() {
        when(merchantRepository.findByUserId(testUserId)).thenReturn(Optional.of(testMerchant));
        when(transactionRepository.save(any(QrisTransaction.class))).thenReturn(testTransaction);

        QrisResponse response = qrisService.generateQris(testUserId, generateRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isInstanceOf(QrisResponse.TransactionData.class);

        QrisResponse.TransactionData data = (QrisResponse.TransactionData) response.getData();
        assertThat(data.getId()).isEqualTo(testTransactionId);
        assertThat(data.getAmount()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(data.getCustomerReference()).isEqualTo("INV-001");
        assertThat(data.getStatus()).isEqualTo("PENDING");

        verify(merchantRepository).findByUserId(testUserId);
        verify(transactionRepository).save(any(QrisTransaction.class));
    }

    @Test
    @DisplayName("generateQris - should throw exception when merchant not found")
    void generateQris_MerchantNotFound_ThrowsException() {
        when(merchantRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> qrisService.generateQris(testUserId, generateRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Merchant not found");

        verify(merchantRepository).findByUserId(testUserId);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("generateQris - should throw exception when merchant is not active")
    void generateQris_MerchantNotActive_ThrowsException() {
        testMerchant.setIsActive(false);
        when(merchantRepository.findByUserId(testUserId)).thenReturn(Optional.of(testMerchant));

        assertThatThrownBy(() -> qrisService.generateQris(testUserId, generateRequest))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Merchant is not active");

        verify(merchantRepository).findByUserId(testUserId);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("getTransactions - should return transaction list")
    void getTransactions_Success() {
        QrisTransaction secondTransaction = new QrisTransaction();
        secondTransaction.setId(101L);
        secondTransaction.setMerchantId(testMerchantId);
        secondTransaction.setTransactionId("QRIS0987654321");
        secondTransaction.setAmount(new BigDecimal("25000"));
        secondTransaction.setStatus("SETTLEMENT");

        when(merchantRepository.findByUserId(testUserId)).thenReturn(Optional.of(testMerchant));
        when(transactionRepository.findByMerchantIdOrderByCreatedAtDesc(testMerchantId))
            .thenReturn(Arrays.asList(testTransaction, secondTransaction));

        QrisResponse response = qrisService.getTransactions(testUserId);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getData()).isInstanceOf(List.class);

        @SuppressWarnings("unchecked")
        List<QrisResponse.TransactionData> transactionList = (List<QrisResponse.TransactionData>) response.getData();
        assertThat(transactionList).hasSize(2);

        verify(merchantRepository).findByUserId(testUserId);
        verify(transactionRepository).findByMerchantIdOrderByCreatedAtDesc(testMerchantId);
    }

    @Test
    @DisplayName("getTransactions - should throw exception when merchant not found")
    void getTransactions_MerchantNotFound_ThrowsException() {
        when(merchantRepository.findByUserId(testUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> qrisService.getTransactions(testUserId))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Merchant not found");

        verify(merchantRepository).findByUserId(testUserId);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    @DisplayName("getTransactions - should return empty list when no transactions")
    void getTransactions_NoTransactions_ReturnsEmptyList() {
        when(merchantRepository.findByUserId(testUserId)).thenReturn(Optional.of(testMerchant));
        when(transactionRepository.findByMerchantIdOrderByCreatedAtDesc(testMerchantId))
            .thenReturn(Arrays.asList());

        QrisResponse response = qrisService.getTransactions(testUserId);

        assertThat(response).isNotNull();
        @SuppressWarnings("unchecked")
        List<QrisResponse.TransactionData> transactionList = (List<QrisResponse.TransactionData>) response.getData();
        assertThat(transactionList).isEmpty();
    }

    @Test
    @DisplayName("activate - should set isActive true on activation")
    void activate_SetsMerchantActive() {
        when(merchantRepository.existsByUserId(testUserId)).thenReturn(false);
        when(merchantRepository.existsByNmid("ID123456")).thenReturn(false);
        when(merchantRepository.save(any(QrisMerchant.class))).thenReturn(testMerchant);

        QrisResponse response = qrisService.activate(testUserId, activateRequest);

        ArgumentCaptor<QrisMerchant> merchantCaptor = ArgumentCaptor.forClass(QrisMerchant.class);
        verify(merchantRepository).save(merchantCaptor.capture());
        QrisMerchant savedMerchant = merchantCaptor.getValue();

        assertThat(savedMerchant.getIsActive()).isTrue();
        assertThat(savedMerchant.getMerchantName()).isEqualTo("Test Merchant");
    }
}
