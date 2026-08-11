package com.tradex.trading_service.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class StockResponse {

    private Long id;

    private String symbol;

    private String companyName;

    private String sector;

    private String exchangeName;

    private BigDecimal currentPrice;

    private BigDecimal previousClose;

    private Boolean active;
}
