package com.tradex.stock_service.repository;

import com.tradex.stock_service.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock,Long> {

    boolean existsBySymbolIgnoreCase(String symbol);

    Optional<Stock> findBySymbolIgnoreCase(String symbol);

    List<Stock> findBySymbolContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(
            String symbolKeyword,
            String companyKeyword
    );
}
