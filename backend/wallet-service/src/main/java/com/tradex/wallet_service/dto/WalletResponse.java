package com.tradex.wallet_service.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// This DTO is returned whenever wallet details are requested.
@Getter
@Setter
@Builder
public class WalletResponse {

    private Long id;

    private Long userId;

    private BigDecimal balance;

    private BigDecimal blockedAmount;

    private BigDecimal availableBalance;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
