package com.tradex.portfolio_service.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
// Used when stock price changes and we want to update current price in holding.
@Getter
@Setter
public class UpdateHoldingPriceRequest {

    @NotNull(message = "Current price is required")
    @DecimalMin(value = "1.0", message = "Current price must be greater than 0")
    private BigDecimal currentPrice;
}
