package com.tradex.trading_service.repository;

import com.tradex.trading_service.entity.Order;
import com.tradex.trading_service.enums.OrderStatus;
import com.tradex.trading_service.enums.TradeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findByUserIdAndTradeTypeOrderByCreatedAtDesc(Long userId, TradeType tradeType);

    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            OrderStatus status
    );

}
