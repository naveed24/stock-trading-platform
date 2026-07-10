package com.tradex.wallet_service.dto;


import com.tradex.wallet_service.enums.WalletTransactionType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// This DTO is returned for wallet transaction history.
@Getter
@Setter
@Builder
public class WalletTransactionResponse {
    private Long id;

    private Long userId;

    private WalletTransactionType transactionType;

    private BigDecimal amount;

    private BigDecimal balanceAfterTransaction;

    private String description;

    private LocalDateTime createdAt;
}
