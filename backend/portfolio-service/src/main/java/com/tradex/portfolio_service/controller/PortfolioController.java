package com.tradex.portfolio_service.controller;

import com.tradex.portfolio_service.service.PortfolioService;

// Import your DTOs from their actual package
import com.tradex.portfolio_service.dto.HoldingResponse;
import com.tradex.portfolio_service.dto.PortfolioSummaryResponse;
import com.tradex.portfolio_service.dto.PortfolioTransactionResponse;
import com.tradex.portfolio_service.dto.PortfolioUpdateRequest;
import com.tradex.portfolio_service.dto.UpdateHoldingPriceRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;



@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    // Status API.
    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("Portfolio Service is running successfully");
    }

    // Get portfolio summary of a user.
    @GetMapping("/{userId}")
    public ResponseEntity<PortfolioSummaryResponse> getPortfolioSummary(@PathVariable Long userId) {
        return ResponseEntity.ok(portfolioService.getPortfolioSummary(userId));
    }

    // Get one holding.
    @GetMapping("/{userId}/holdings/{stockSymbol}")
    public ResponseEntity<HoldingResponse> getHolding(
            @PathVariable Long userId,
            @PathVariable String stockSymbol
    ) {
        return ResponseEntity.ok(portfolioService.getHolding(userId, stockSymbol));
    }

    // Update holding after BUY or SELL.
    @PostMapping("/update-holding")
    public ResponseEntity<PortfolioSummaryResponse> updateHolding(
            @Valid @RequestBody PortfolioUpdateRequest request
    ) {
        return ResponseEntity.ok(portfolioService.updateHolding(request));
    }

    // Update current price for a user's holding.
    @PutMapping("/{userId}/holdings/{stockSymbol}/price")
    public ResponseEntity<HoldingResponse> updateCurrentPrice(
            @PathVariable Long userId,
            @PathVariable String stockSymbol,
            @Valid @RequestBody UpdateHoldingPriceRequest request
    ) {
        return ResponseEntity.ok(portfolioService.updateCurrentPrice(userId, stockSymbol, request));
    }

    // Get portfolio transaction history.
    @GetMapping("/transactions/{userId}")
    public ResponseEntity<List<PortfolioTransactionResponse>> getTransactions(@PathVariable Long userId) {
        return ResponseEntity.ok(portfolioService.getTransactions(userId));
    }

}
