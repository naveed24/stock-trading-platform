package com.tradex.stock_service.controller;

import com.tradex.stock_service.dto.CreateStockRequest;
import com.tradex.stock_service.dto.StockResponse;
import com.tradex.stock_service.dto.UpdateStockPriceRequest;
import com.tradex.stock_service.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("Stock Service is running successfully");
    }

    @PostMapping
    public ResponseEntity<StockResponse> createStock(@Valid @RequestBody CreateStockRequest request) {
        return ResponseEntity.ok(stockService.createStock(request));
    }

    @GetMapping
    public ResponseEntity<List<StockResponse>> getAllStocks() {
        return ResponseEntity.ok(stockService.getAllStocks());
    }

    @GetMapping("/search")
    public ResponseEntity<List<StockResponse>> searchStocks(@RequestParam String keyword) {
        return ResponseEntity.ok(stockService.searchStocks(keyword));
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<StockResponse> getStockBySymbol(@PathVariable String symbol) {
        return ResponseEntity.ok(stockService.getStockBySymbol(symbol));
    }

    @PutMapping("/{symbol}/price")
    public ResponseEntity<StockResponse> updateStockPrice(
            @PathVariable String symbol,
            @Valid @RequestBody UpdateStockPriceRequest request
    ) {
        return ResponseEntity.ok(stockService.updateStockPrice(symbol, request));
    }

    @DeleteMapping("/{symbol}")
    public ResponseEntity<String> deleteStock(@PathVariable String symbol) {
        return ResponseEntity.ok(stockService.deleteStock(symbol));
    }






}
