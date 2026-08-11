package com.tradex.trading_service.dto;


import com.tradex.trading_service.enums.OrderStatus;
import com.tradex.trading_service.enums.OrderType;
import com.tradex.trading_service.enums.TradeType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class OrderResponse {

    private Long orderId;

    private Long userId;

    private String stockSymbol;

    private TradeType tradeType;

    private OrderType orderType;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal totalAmount;

    private OrderStatus status;

    private String failureReason;

    private LocalDateTime createdAt;

    private LocalDateTime executedAt;
}
