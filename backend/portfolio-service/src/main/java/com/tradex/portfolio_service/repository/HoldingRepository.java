package com.tradex.portfolio_service.repository;

import com.tradex.portfolio_service.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HoldingRepository extends JpaRepository<Holding,Long> {

    Optional<Holding> findByUserIdAndStockSymbolIgnoreCase(Long userId, String stockSymbol);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from Holding h where h.userId = :userId and upper(h.stockSymbol) = upper(:symbol)")
    Optional<Holding> findForUpdate(@Param("userId") Long userId, @Param("symbol") String stockSymbol);
    List<Holding> findByUserIdOrderByStockSymbolAsc(Long userId);
}
