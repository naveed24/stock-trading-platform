package com.tradex.wallet_service.service;

import com.tradex.wallet_service.dto.AmountRequest;
import com.tradex.wallet_service.dto.WalletResponse;
import com.tradex.wallet_service.entity.Wallet;
import com.tradex.wallet_service.repository.WalletRepository;
import com.tradex.wallet_service.repository.WalletTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {
    @Mock WalletRepository walletRepository;
    @Mock WalletTransactionRepository walletTransactionRepository;
    @InjectMocks WalletService service;

    @Test
    void blockAmountReservesFundsWithoutChangingBalance() {
        Wallet wallet = Wallet.builder().id(1L).userId(7L)
                .balance(new BigDecimal("1000.00"))
                .blockedAmount(BigDecimal.ZERO).build();
        when(walletRepository.findByUserIdForUpdate(7L)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        AmountRequest request = new AmountRequest();
        request.setUserId(7L);
        request.setAmount(new BigDecimal("250.00"));

        WalletResponse response = service.blockAmount(request);

        assertEquals(0, new BigDecimal("1000.00").compareTo(response.getBalance()));
        assertEquals(0, new BigDecimal("250.00").compareTo(response.getBlockedAmount()));
        assertEquals(0, new BigDecimal("750.00").compareTo(response.getAvailableBalance()));
        verify(walletRepository).findByUserIdForUpdate(7L);
    }

    @Test
    void cannotReserveMoreThanAvailableFunds() {
        Wallet wallet = Wallet.builder().id(1L).userId(7L)
                .balance(new BigDecimal("100.00"))
                .blockedAmount(new BigDecimal("80.00")).build();
        when(walletRepository.findByUserIdForUpdate(7L)).thenReturn(Optional.of(wallet));
        AmountRequest request = new AmountRequest();
        request.setUserId(7L);
        request.setAmount(new BigDecimal("30.00"));

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> service.blockAmount(request));
        verify(walletRepository, never()).save(any(Wallet.class));
    }
}
