package com.tradex.stock_service.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateStockRequest {


    @NotBlank(message = "Stock symbol is required")
    private String symbol;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Sector is required")
    private String sector;

    @NotBlank(message = "Exchange name is required")
    private String exchangeName;

    @NotNull(message = "Current price is required")
    @DecimalMin(value = "1.0", message = "Current price must be greater than 0")
    private BigDecimal currentPrice;

    @NotNull(message = "Previous close price is required")
    @DecimalMin(value = "1.0", message = "Previous close price must be greater than 0")
    private BigDecimal previousClose;


}
