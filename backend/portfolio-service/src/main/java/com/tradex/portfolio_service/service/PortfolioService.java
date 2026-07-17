package com.tradex.portfolio_service.service;


import com.tradex.portfolio_service.dto.PortfolioSummaryResponse;
import com.tradex.portfolio_service.dto.PortfolioUpdateRequest;
import com.tradex.portfolio_service.repository.HoldingRepository;
import com.tradex.portfolio_service.repository.PortfolioTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final HoldingRepository holdingRepository;
    private final PortfolioTransactionRepository transactionRepository;

    @Transactional
    public PortfolioSummaryResponse updateHolding(PortfolioUpdateRequest request){
        String stockSymbol=request
    }
}
