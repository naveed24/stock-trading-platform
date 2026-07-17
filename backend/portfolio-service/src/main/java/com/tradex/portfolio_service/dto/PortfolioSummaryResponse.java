package com.tradex.portfolio_service.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class PortfolioSummaryResponse {

    private Long userId;

    private BigDecimal totalInvested;

    private BigDecimal currentValue;

    private BigDecimal totalProfitLoss;

    private BigDecimal totalProfitLossPercentage;

    private List<HoldingResponse> holdings;
}
