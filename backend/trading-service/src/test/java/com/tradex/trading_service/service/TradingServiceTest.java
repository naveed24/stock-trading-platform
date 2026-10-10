package com.tradex.trading_service.service;

import com.tradex.trading_service.client.PortfolioClient;
import com.tradex.trading_service.client.StockClient;
import com.tradex.trading_service.client.WalletClient;
import com.tradex.trading_service.dto.BuyOrderRequest;
import com.tradex.trading_service.dto.HoldingResponse;
import com.tradex.trading_service.dto.OrderResponse;
import com.tradex.trading_service.dto.PortfolioUpdateRequest;
import com.tradex.trading_service.dto.SellOrderRequest;
import com.tradex.trading_service.dto.StockResponse;
import com.tradex.trading_service.dto.WalletRequest;
import com.tradex.trading_service.entity.Order;
import com.tradex.trading_service.enums.OrderStatus;
import com.tradex.trading_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradingServiceTest {
    @Mock OrderRepository orderRepository;
    @Mock StockClient stockClient;
    @Mock WalletClient walletClient;
    @Mock PortfolioClient portfolioClient;
    @InjectMocks TradingService service;

    @BeforeEach
    void setup() {
        lenient().when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) order.setId(1L);
            return order;
        });
    }

    private void stockAt(String symbol, String price) {
        StockResponse stock = mock(StockResponse.class);
        when(stockClient.getStock(symbol)).thenReturn(stock);
        when(stock.getCurrentPrice()).thenReturn(new BigDecimal(price));
        lenient().when(stock.getSymbol()).thenReturn(symbol);
        lenient().when(stock.getActive()).thenReturn(true);
    }

    @Test
    void buyBlocksFundsUpdatesPortfolioThenDebits() {
        stockAt("TCS", "100.00");
        BuyOrderRequest request = new BuyOrderRequest();
        request.setUserId(5L);
        request.setStockSymbol("tcs");
        request.setQuantity(2);

        OrderResponse result = service.buyStock(request);

        assertEquals(OrderStatus.EXECUTED, result.getStatus());
        assertEquals(0, new BigDecimal("200.00").compareTo(result.getTotalAmount()));
        var order = inOrder(walletClient, portfolioClient);
        order.verify(walletClient).blockAmount(any(WalletRequest.class));
        order.verify(portfolioClient).updateHolding(any(PortfolioUpdateRequest.class));
        order.verify(walletClient).debitAmount(any(WalletRequest.class));
    }

    @Test
    void portfolioFailureReleasesReservedFunds() {
        stockAt("TCS", "100.00");
        BuyOrderRequest request = new BuyOrderRequest();
        request.setUserId(5L);
        request.setStockSymbol("tcs");
        request.setQuantity(1);
        doThrow(new RuntimeException("portfolio error"))
                .when(portfolioClient).updateHolding(any(PortfolioUpdateRequest.class));

        assertThrows(RuntimeException.class, () -> service.buyStock(request));
        verify(walletClient).blockAmount(any(WalletRequest.class));
        verify(walletClient).releaseAmount(any(WalletRequest.class));
        verify(walletClient, never()).debitAmount(any(WalletRequest.class));
    }

    @Test
    void failedSellCreditsCompensateHoldingsBestEffort() {
        stockAt("TCS", "100.00");
        HoldingResponse holding = mock(HoldingResponse.class);
        when(holding.getQuantity()).thenReturn(5);
        when(holding.getAverageBuyPrice()).thenReturn(new BigDecimal("80.00"));
        when(portfolioClient.getHolding(5L, "TCS")).thenReturn(holding);
        doThrow(new RuntimeException("wallet credit unavailable"))
                .when(walletClient).creditAmount(any(WalletRequest.class));

        SellOrderRequest request = new SellOrderRequest();
        request.setUserId(5L);
        request.setStockSymbol("TCS");
        request.setQuantity(2);

        assertThrows(RuntimeException.class, () -> service.sellStock(request));
        ArgumentCaptor<PortfolioUpdateRequest> compensation =
                ArgumentCaptor.forClass(PortfolioUpdateRequest.class);
        verify(portfolioClient, times(2)).updateHolding(compensation.capture());
        assertEquals("SELL", compensation.getAllValues().get(0).getTradeType());
        assertEquals("BUY", compensation.getAllValues().get(1).getTradeType());
    }
}
