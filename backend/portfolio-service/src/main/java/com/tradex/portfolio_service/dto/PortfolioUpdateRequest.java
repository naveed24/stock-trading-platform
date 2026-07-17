package com.tradex.portfolio_service.dto;


import com.tradex.portfolio_service.enums.TradeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


// This request will be called by trading-service after buy/sell execution.
@Getter
@Setter
public class PortfolioUpdateRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotBlank(message = "Stock symbol is required")
    private String stockSymbol;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "1.0", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Trade type is required")
    private TradeType tradeType;
}
