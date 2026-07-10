package com.tradex.wallet_service.dto;


// This DTO is used for deposit, withdraw, block, debit, credit operations.

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

// This DTO is used for deposit, withdraw, block, debit, credit operations.
@Getter
@Setter
public class AmountRequest {

    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Amount must be greater than 0")
    private BigDecimal amount;
}
