package com.tradex.portfolio_service.repository;

import com.tradex.portfolio_service.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding,Long> {

    Optional<Holding> findByUserIdAndStockSymbolIgnoreCase(Long userId, String stockSymbol);
    List<Holding> findByUserIdOrderByStockSymbolAsc(Long userId);
}
