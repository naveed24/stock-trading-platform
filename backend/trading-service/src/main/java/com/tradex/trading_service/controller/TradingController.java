package com.tradex.trading_service.controller;


import com.tradex.trading_service.dto.BuyOrderRequest;
import com.tradex.trading_service.dto.OrderResponse;
import com.tradex.trading_service.dto.SellOrderRequest;
import com.tradex.trading_service.enums.OrderStatus;
import com.tradex.trading_service.enums.TradeType;
import com.tradex.trading_service.service.TradingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class TradingController {

    private final TradingService tradingService;

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok(
                "Trading Service is running successfully"
        );
    }

    @PostMapping("/buy")
    public ResponseEntity<OrderResponse> buyStock(
            @Valid @RequestBody BuyOrderRequest request
    ) {

        return ResponseEntity.ok(
                tradingService.buyStock(request)
        );
    }

    @PostMapping("/sell")
    public ResponseEntity<OrderResponse> sellStock(
            @Valid @RequestBody SellOrderRequest request
    ) {

        return ResponseEntity.ok(
                tradingService.sellStock(request)
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUser(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                tradingService.getOrdersByUser(userId)
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId
    ) {

        return ResponseEntity.ok(
                tradingService.getOrderById(orderId)
        );
    }

    @GetMapping("/user/{userId}/type/{tradeType}")
    public ResponseEntity<List<OrderResponse>> getOrdersByTradeType(
            @PathVariable Long userId,
            @PathVariable TradeType tradeType
    ) {

        return ResponseEntity.ok(
                tradingService.getOrdersByTradeType(
                        userId,
                        tradeType
                )
        );
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(
            @PathVariable Long userId,
            @PathVariable OrderStatus status
    ) {

        return ResponseEntity.ok(
                tradingService.getOrdersByStatus(
                        userId,
                        status
                )
        );
    }



}
