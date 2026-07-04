package com.tradex.stock_service.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class StockResponse {

    private Long id;

    private String symbol;

    private String companyName;

    private String sector;

    private String exchangeName;

    private BigDecimal currentPrice;

    private BigDecimal previousClose;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
