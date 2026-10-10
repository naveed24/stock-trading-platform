package com.tradex.trading_service.service;


import com.tradex.trading_service.client.PortfolioClient;
import com.tradex.trading_service.client.StockClient;
import com.tradex.trading_service.client.WalletClient;
import com.tradex.trading_service.dto.*;
import com.tradex.trading_service.entity.Order;
import com.tradex.trading_service.enums.OrderStatus;
import com.tradex.trading_service.enums.OrderType;
import com.tradex.trading_service.enums.TradeType;
import com.tradex.trading_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradingService {

    private final OrderRepository orderRepository;

    private final StockClient stockClient;

    private final WalletClient walletClient;

    private final PortfolioClient portfolioClient;


    public OrderResponse buyStock(BuyOrderRequest request) {

        System.out.println("===== BUY ORDER STARTED =====");

        String symbol = request.getStockSymbol().toUpperCase();

        System.out.println("STEP 1: Fetching stock");

        StockResponse stock = stockClient.getStock(symbol);

        System.out.println(
                "STEP 1 SUCCESS: " +
                        stock.getSymbol() +
                        " Price = " +
                        stock.getCurrentPrice()
        );

        BigDecimal price = stock.getCurrentPrice();

        BigDecimal totalAmount =
                price.multiply(
                        BigDecimal.valueOf(request.getQuantity())
                );

        System.out.println(
                "STEP 2: Total amount = " + totalAmount
        );

        Order order = Order.builder()
                .userId(request.getUserId())
                .stockSymbol(symbol)
                .tradeType(TradeType.BUY)
                .orderType(OrderType.MARKET)
                .quantity(request.getQuantity())
                .price(price)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .build();

        System.out.println("STEP 3: Saving PENDING order");

        order = orderRepository.save(order);

        System.out.println(
                "STEP 3 SUCCESS: Order ID = " + order.getId()
        );

        boolean amountBlocked = false;
        boolean portfolioUpdated = false;
        boolean walletDebited = false;

        try {

            WalletRequest walletRequest =
                    new WalletRequest(
                            request.getUserId(),
                            totalAmount
                    );

            System.out.println("STEP 4: Blocking wallet amount");

            walletClient.blockAmount(walletRequest);

            amountBlocked = true;

            System.out.println("STEP 4 SUCCESS");

            PortfolioUpdateRequest portfolioRequest =
                    new PortfolioUpdateRequest(
                            request.getUserId(),
                            symbol,
                            request.getQuantity(),
                            price,
                            "BUY"
                    );

            System.out.println("STEP 5: Updating portfolio");

            portfolioClient.updateHolding(portfolioRequest);
            portfolioUpdated = true;

            System.out.println("STEP 5 SUCCESS");

            System.out.println("STEP 6: Debiting wallet");

            walletClient.debitAmount(walletRequest);
            walletDebited = true;

            System.out.println("STEP 6 SUCCESS");

            amountBlocked = false;

            order.setStatus(OrderStatus.EXECUTED);
            order.setExecutedAt(LocalDateTime.now());

            order = orderRepository.save(order);

            System.out.println("STEP 7 SUCCESS: ORDER EXECUTED");
            System.out.println("===== BUY ORDER COMPLETED =====");

            return mapToResponse(order);

        } catch (Exception exception) {

            System.out.println("===== BUY ORDER FAILED =====");
            System.out.println(
                    "ERROR: " + exception.getMessage()
            );

            exception.printStackTrace();

            // Best-effort compensation for failures confirmed before wallet debit.
            // Remote timeouts after a successful call still need durable reconciliation.
            if (portfolioUpdated && !walletDebited) {
                try {
                    portfolioClient.updateHolding(new PortfolioUpdateRequest(
                            request.getUserId(), symbol, request.getQuantity(), price, "SELL"));
                } catch (Exception rollbackFailure) {
                    System.err.println("Manual reconciliation required for buy order "
                            + order.getId() + ": " + rollbackFailure.getMessage());
                }
            }

            if (amountBlocked) {
                try {
                    System.out.println(
                            "Releasing blocked amount..."
                    );

                    walletClient.releaseAmount(
                            new WalletRequest(
                                    request.getUserId(),
                                    totalAmount
                            )
                    );

                    System.out.println(
                            "Blocked amount released successfully"
                    );

                } catch (Exception releaseException) {

                    System.out.println(
                            "FAILED TO RELEASE AMOUNT: " +
                                    releaseException.getMessage()
                    );
                }
            }

            order.setStatus(OrderStatus.FAILED);
            order.setFailureReason(exception.getMessage());

            orderRepository.save(order);

            throw new RuntimeException(
                    "Buy order failed: " +
                            exception.getMessage()
            );
        }
    }


    public OrderResponse sellStock(SellOrderRequest request) {

        System.out.println("===== SELL ORDER STARTED =====");

        // ------------------------------------------------
        // STEP 1: Normalize symbol
        // ------------------------------------------------

        String symbol = request.getStockSymbol().toUpperCase();

        // ------------------------------------------------
        // STEP 2: Get current stock information
        // ------------------------------------------------

        System.out.println("SELL STEP 1: Fetching stock");

        StockResponse stock = stockClient.getStock(symbol);

        if (stock == null) {
            throw new RuntimeException("Stock not found");
        }

        if (Boolean.FALSE.equals(stock.getActive())) {
            throw new RuntimeException(
                    "Trading is disabled for stock: " + symbol
            );
        }

        BigDecimal price = stock.getCurrentPrice();

        System.out.println(
                "SELL STEP 1 SUCCESS: Current price = " + price
        );

        // ------------------------------------------------
        // STEP 3: Check user holding
        // ------------------------------------------------

        System.out.println(
                "SELL STEP 2: Checking portfolio holding"
        );

        HoldingResponse holding =
                portfolioClient.getHolding(
                        request.getUserId(),
                        symbol
                );

        if (holding == null) {
            throw new RuntimeException(
                    "No holding found for stock: " + symbol
            );
        }

        if (holding.getQuantity() < request.getQuantity()) {

            throw new RuntimeException(
                    "Insufficient quantity. Available: "
                            + holding.getQuantity()
                            + ", Requested: "
                            + request.getQuantity()
            );
        }

        System.out.println(
                "SELL STEP 2 SUCCESS: Available quantity = "
                        + holding.getQuantity()
        );

        // ------------------------------------------------
        // STEP 4: Calculate sale amount
        // ------------------------------------------------

        BigDecimal totalAmount =
                price.multiply(
                        BigDecimal.valueOf(
                                request.getQuantity()
                        )
                );

        System.out.println(
                "SELL STEP 3: Total sale value = "
                        + totalAmount
        );

        // ------------------------------------------------
        // STEP 5: Create PENDING order
        // ------------------------------------------------

        Order order = Order.builder()
                .userId(request.getUserId())
                .stockSymbol(symbol)
                .tradeType(TradeType.SELL)
                .orderType(OrderType.MARKET)
                .quantity(request.getQuantity())
                .price(price)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .build();

        order = orderRepository.save(order);

        System.out.println(
                "SELL STEP 4 SUCCESS: Order ID = "
                        + order.getId()
        );

        boolean portfolioUpdated = false;

        try {

            // ------------------------------------------------
            // STEP 6: Reduce portfolio quantity
            // ------------------------------------------------

            PortfolioUpdateRequest portfolioRequest =
                    new PortfolioUpdateRequest(
                            request.getUserId(),
                            symbol,
                            request.getQuantity(),
                            price,
                            "SELL"
                    );

            System.out.println(
                    "SELL STEP 5: Updating portfolio"
            );

            portfolioClient.updateHolding(
                    portfolioRequest
            );

            portfolioUpdated = true;

            System.out.println(
                    "SELL STEP 5 SUCCESS"
            );

            // ------------------------------------------------
            // STEP 7: Credit money to wallet
            // ------------------------------------------------

            WalletRequest walletRequest =
                    new WalletRequest(
                            request.getUserId(),
                            totalAmount
                    );

            System.out.println(
                    "SELL STEP 6: Crediting wallet"
            );

            walletClient.creditAmount(
                    walletRequest
            );

            System.out.println(
                    "SELL STEP 6 SUCCESS"
            );

            // ------------------------------------------------
            // STEP 8: Mark order EXECUTED
            // ------------------------------------------------

            order.setStatus(
                    OrderStatus.EXECUTED
            );

            order.setExecutedAt(
                    LocalDateTime.now()
            );

            order = orderRepository.save(order);

            System.out.println(
                    "SELL STEP 7 SUCCESS: ORDER EXECUTED"
            );

            System.out.println(
                    "===== SELL ORDER COMPLETED ====="
            );

            return mapToResponse(order);

        } catch (Exception exception) {

            System.out.println(
                    "===== SELL ORDER FAILED ====="
            );

            System.out.println(
                    "ERROR: " + exception.getMessage()
            );

            /*
             * IMPORTANT:
             *
             * If Portfolio already reduced the quantity
             * but Wallet credit failed, the system is now
             * partially updated.
             *
             * We will solve this properly later using
             * Saga + Kafka + compensation events.
             */

            if (portfolioUpdated) {
                // Restore the original acquisition price when attempting a rollback.
                try {
                    portfolioClient.updateHolding(new PortfolioUpdateRequest(
                            request.getUserId(), symbol, request.getQuantity(),
                            holding.getAverageBuyPrice(), "BUY"));
                } catch (Exception rollbackFailure) {
                    System.err.println("Manual reconciliation required for sell order "
                            + order.getId() + ": " + rollbackFailure.getMessage());
                }
            }

            order.setStatus(
                    OrderStatus.FAILED
            );

            order.setFailureReason(
                    exception.getMessage()
            );

            orderRepository.save(order);

            throw new RuntimeException(
                    "Sell order failed: "
                            + exception.getMessage()
            );
        }


    }
    public List<OrderResponse> getOrdersByUser(Long userId) {

        return orderRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrderResponse getOrderById(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found with id: " + orderId
                        )
                );

        return mapToResponse(order);
    }

    public List<OrderResponse> getOrdersByTradeType(
            Long userId,
            TradeType tradeType
    ) {

        return orderRepository
                .findByUserIdAndTradeTypeOrderByCreatedAtDesc(
                        userId,
                        tradeType
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<OrderResponse> getOrdersByStatus(
            Long userId,
            OrderStatus status
    ) {

        return orderRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(
                        userId,
                        status
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }




    private OrderResponse mapToResponse(Order order) {

        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .stockSymbol(order.getStockSymbol())
                .tradeType(order.getTradeType())
                .orderType(order.getOrderType())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .failureReason(order.getFailureReason())
                .createdAt(order.getCreatedAt())
                .executedAt(order.getExecutedAt())
                .build();
    }
}
