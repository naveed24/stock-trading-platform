package com.tradex.trading_service.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class WalletRequest {

    private Long userId;

    private BigDecimal amount;
}
