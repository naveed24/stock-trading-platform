package com.tradex.trading_service.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class PortfolioUpdateRequest {

    private Long userId;

    private String stockSymbol;

    private Integer quantity;

    private BigDecimal price;

    private String tradeType;
}
