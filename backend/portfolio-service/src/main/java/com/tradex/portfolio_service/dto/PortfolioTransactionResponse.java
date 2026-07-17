package com.tradex.portfolio_service.dto;

// Response for portfolio transaction history.


import com.tradex.portfolio_service.enums.TradeType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PortfolioTransactionResponse {


    private Long id;

    private Long userId;

    private String stockSymbol;

    private TradeType tradeType;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal totalAmount;

    private LocalDateTime createdAt;

}
