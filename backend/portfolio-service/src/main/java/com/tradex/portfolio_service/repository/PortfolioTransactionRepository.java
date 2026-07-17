package com.tradex.portfolio_service.repository;

import com.tradex.portfolio_service.entity.PortfolioTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioTransactionRepository extends JpaRepository<PortfolioTransaction,Long> {

    List<PortfolioTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

}
