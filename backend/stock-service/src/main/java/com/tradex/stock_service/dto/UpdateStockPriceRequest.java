package com.tradex.stock_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateStockPriceRequest {

    @NotNull(message = "New price is required")
    @DecimalMin(value = "1.0", message = "Price must be greater than 0")
    private BigDecimal newPrice;
}
