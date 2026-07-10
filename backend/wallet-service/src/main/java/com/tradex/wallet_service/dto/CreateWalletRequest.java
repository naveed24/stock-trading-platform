package com.tradex.wallet_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// This DTO is used to create wallet for a user.
@Getter
@Setter
public class CreateWalletRequest {

    @NotNull(message="User id is required")
    private Long userId;
}
