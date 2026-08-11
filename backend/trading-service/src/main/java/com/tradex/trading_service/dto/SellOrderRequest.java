package com.tradex.trading_service.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellOrderRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotBlank(message = "Stock symbol is required")
    private String stockSymbol;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
