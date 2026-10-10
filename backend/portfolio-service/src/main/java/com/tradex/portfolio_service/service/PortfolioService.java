package com.tradex.portfolio_service.service;


import com.tradex.portfolio_service.dto.*;
import com.tradex.portfolio_service.entity.Holding;
import com.tradex.portfolio_service.entity.PortfolioTransaction;
import com.tradex.portfolio_service.enums.TradeType;
import com.tradex.portfolio_service.repository.HoldingRepository;
import com.tradex.portfolio_service.repository.PortfolioTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final HoldingRepository holdingRepository;
    private final PortfolioTransactionRepository transactionRepository;

    // This method updates holding after BUY or SELL.
    @Transactional
    public PortfolioSummaryResponse updateHolding(PortfolioUpdateRequest request) {

        String stockSymbol = request.getStockSymbol().toUpperCase();

        if (request.getTradeType() == TradeType.BUY) {
            handleBuy(request, stockSymbol);
        } else if (request.getTradeType() == TradeType.SELL) {
            handleSell(request, stockSymbol);
        } else {
            throw new RuntimeException("Invalid trade type");
        }

        saveTransaction(request, stockSymbol);

        return getPortfolioSummary(request.getUserId());
    }


    // Handles BUY operation.
    private void handleBuy(PortfolioUpdateRequest request, String stockSymbol) {

        Holding holding = holdingRepository
                .findForUpdate(request.getUserId(), stockSymbol)
                .orElse(null);

        // If user is buying this stock for the first time.
        if (holding == null) {
            Holding newHolding = Holding.builder()
                    .userId(request.getUserId())
                    .stockSymbol(stockSymbol)
                    .quantity(request.getQuantity())
                    .averageBuyPrice(request.getPrice())
                    .currentPrice(request.getPrice())
                    .build();

            holdingRepository.save(newHolding);
            return;
        }

        // If user already owns this stock, calculate new average buy price.
        BigDecimal oldTotalAmount = holding.getAverageBuyPrice()
                .multiply(BigDecimal.valueOf(holding.getQuantity()));

        BigDecimal newBuyAmount = request.getPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        int newTotalQuantity = holding.getQuantity() + request.getQuantity();

        BigDecimal newAveragePrice = oldTotalAmount
                .add(newBuyAmount)
                .divide(BigDecimal.valueOf(newTotalQuantity), 2, RoundingMode.HALF_UP);

        holding.setQuantity(newTotalQuantity);
        holding.setAverageBuyPrice(newAveragePrice);
        holding.setCurrentPrice(request.getPrice());

        holdingRepository.save(holding);
    }

    // Handles SELL operation.
    private void handleSell(PortfolioUpdateRequest request, String stockSymbol) {

        Holding holding = holdingRepository
                .findForUpdate(request.getUserId(), stockSymbol)
                .orElseThrow(() -> new RuntimeException("No holding found for stock: " + stockSymbol));

        if (holding.getQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock quantity to sell");
        }

        int remainingQuantity = holding.getQuantity() - request.getQuantity();

        // If user sold all shares, remove the holding.
        if (remainingQuantity == 0) {
            holdingRepository.delete(holding);
            return;
        }

        // If partial sell, quantity reduces but average buy price remains same.
        holding.setQuantity(remainingQuantity);
        holding.setCurrentPrice(request.getPrice());

        holdingRepository.save(holding);
    }

    // Get full portfolio summary.
    public PortfolioSummaryResponse getPortfolioSummary(Long userId) {

        List<HoldingResponse> holdings = holdingRepository.findByUserIdOrderByStockSymbolAsc(userId)
                .stream()
                .map(this::mapToHoldingResponse)
                .toList();

        BigDecimal totalInvested = holdings.stream()
                .map(HoldingResponse::getTotalInvested)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentValue = holdings.stream()
                .map(HoldingResponse::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProfitLoss = currentValue.subtract(totalInvested);

        BigDecimal totalProfitLossPercentage = BigDecimal.ZERO;

        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            totalProfitLossPercentage = totalProfitLoss
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalInvested, 2, RoundingMode.HALF_UP);
        }

        return PortfolioSummaryResponse.builder()
                .userId(userId)
                .totalInvested(totalInvested)
                .currentValue(currentValue)
                .totalProfitLoss(totalProfitLoss)
                .totalProfitLossPercentage(totalProfitLossPercentage)
                .holdings(holdings)
                .build();
    }

    // Get one holding.
    public HoldingResponse getHolding(Long userId, String stockSymbol) {

        Holding holding = holdingRepository
                .findByUserIdAndStockSymbolIgnoreCase(userId, stockSymbol)
                .orElseThrow(() -> new RuntimeException("Holding not found for stock: " + stockSymbol));

        return mapToHoldingResponse(holding);
    }

    // Update current price of a specific holding.
    @Transactional
    public HoldingResponse updateCurrentPrice(Long userId, String stockSymbol, UpdateHoldingPriceRequest request) {

        Holding holding = holdingRepository
                .findByUserIdAndStockSymbolIgnoreCase(userId, stockSymbol)
                .orElseThrow(() -> new RuntimeException("Holding not found for stock: " + stockSymbol));

        holding.setCurrentPrice(request.getCurrentPrice());

        Holding updatedHolding = holdingRepository.save(holding);

        return mapToHoldingResponse(updatedHolding);
    }

    // Get portfolio transaction history.
    public List<PortfolioTransactionResponse> getTransactions(Long userId) {

        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToTransactionResponse)
                .toList();
    }

    // Save BUY/SELL transaction history.
    private void saveTransaction(PortfolioUpdateRequest request, String stockSymbol) {

        BigDecimal totalAmount = request.getPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()));

        PortfolioTransaction transaction = PortfolioTransaction.builder()
                .userId(request.getUserId())
                .stockSymbol(stockSymbol)
                .tradeType(request.getTradeType())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .totalAmount(totalAmount)
                .build();

        transactionRepository.save(transaction);
    }

    // Convert Holding entity into response DTO.
    private HoldingResponse mapToHoldingResponse(Holding holding) {

        BigDecimal totalInvested = holding.getAverageBuyPrice()
                .multiply(BigDecimal.valueOf(holding.getQuantity()));

        BigDecimal currentValue = holding.getCurrentPrice()
                .multiply(BigDecimal.valueOf(holding.getQuantity()));

        BigDecimal profitLoss = currentValue.subtract(totalInvested);

        BigDecimal profitLossPercentage = BigDecimal.ZERO;

        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            profitLossPercentage = profitLoss
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalInvested, 2, RoundingMode.HALF_UP);
        }

        return HoldingResponse.builder()
                .id(holding.getId())
                .userId(holding.getUserId())
                .stockSymbol(holding.getStockSymbol())
                .quantity(holding.getQuantity())
                .averageBuyPrice(holding.getAverageBuyPrice())
                .currentPrice(holding.getCurrentPrice())
                .totalInvested(totalInvested)
                .currentValue(currentValue)
                .profitLoss(profitLoss)
                .profitLossPercentage(profitLossPercentage)
                .build();
    }

    // Convert PortfolioTransaction entity into response DTO.
    private PortfolioTransactionResponse mapToTransactionResponse(PortfolioTransaction transaction) {

        return PortfolioTransactionResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUserId())
                .stockSymbol(transaction.getStockSymbol())
                .tradeType(transaction.getTradeType())
                .quantity(transaction.getQuantity())
                .price(transaction.getPrice())
                .totalAmount(transaction.getTotalAmount())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

}
