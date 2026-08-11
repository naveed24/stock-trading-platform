package com.tradex.trading_service.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
public class HoldingResponse {

    private Long id;

    private Long userId;

    private String stockSymbol;

    private Integer quantity;

    private BigDecimal averageBuyPrice;

    private BigDecimal currentPrice;

    private BigDecimal totalInvested;

    private BigDecimal currentValue;

    private BigDecimal profitLoss;

    private BigDecimal profitLossPercentage;
}
