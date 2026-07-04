package com.tradex.stock_service.service;

import com.tradex.stock_service.dto.CreateStockRequest;
import com.tradex.stock_service.dto.StockResponse;
import com.tradex.stock_service.dto.UpdateStockPriceRequest;
import com.tradex.stock_service.entity.Stock;
import com.tradex.stock_service.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;



    public StockResponse createStock(CreateStockRequest request){
        String symbol=request.getSymbol().toUpperCase();

        if (stockRepository.existsBySymbolIgnoreCase(symbol)) {
            throw new RuntimeException("Stock already exists with symbol: " + symbol);
        }

        Stock stock = Stock.builder()
                .symbol(symbol)
                .companyName(request.getCompanyName())
                .sector(request.getSector())
                .exchangeName(request.getExchangeName())
                .currentPrice(request.getCurrentPrice())
                .previousClose(request.getPreviousClose())
                .active(true)
                .build();

        Stock savedStock = stockRepository.save(stock);

        return mapToResponse(savedStock);
    }

    public List<StockResponse> getAllStocks() {
        return stockRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public StockResponse getStockBySymbol(String symbol) {
        Stock stock = stockRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new RuntimeException("Stock not found with symbol: " + symbol));

        return mapToResponse(stock);
    }

    public List<StockResponse> searchStocks(String keyword) {
        return stockRepository
                .findBySymbolContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(keyword, keyword)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public StockResponse updateStockPrice(String symbol, UpdateStockPriceRequest request) {
        Stock stock = stockRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new RuntimeException("Stock not found with symbol: " + symbol));

        // Move current price to previous close.
        stock.setPreviousClose(stock.getCurrentPrice());

        // Set new current price.
        stock.setCurrentPrice(request.getNewPrice());

        Stock updatedStock = stockRepository.save(stock);

        return mapToResponse(updatedStock);
    }

    public String deleteStock(String symbol) {
        Stock stock = stockRepository.findBySymbolIgnoreCase(symbol)
                .orElseThrow(() -> new RuntimeException("Stock not found with symbol: " + symbol));

        stockRepository.delete(stock);

        return "Stock deleted successfully with symbol: " + symbol.toUpperCase();
    }

    private StockResponse mapToResponse(Stock stock) {
        return StockResponse.builder()
                .id(stock.getId())
                .symbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .sector(stock.getSector())
                .exchangeName(stock.getExchangeName())
                .currentPrice(stock.getCurrentPrice())
                .previousClose(stock.getPreviousClose())
                .active(stock.getActive())
                .createdAt(stock.getCreatedAt())
                .updatedAt(stock.getUpdatedAt())
                .build();
    }






}
