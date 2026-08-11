package com.tradex.trading_service.client;


import com.tradex.trading_service.dto.HoldingResponse;
import com.tradex.trading_service.dto.PortfolioUpdateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PortfolioClient {

    private final RestClient restClient;

    public PortfolioClient(
            @Value("${services.portfolio.url}") String portfolioServiceUrl
    ) {

        this.restClient = RestClient.builder()
                .baseUrl(portfolioServiceUrl)
                .build();
    }


    //For Buy
    public void updateHolding(PortfolioUpdateRequest request) {

        restClient.post()
                .uri("/api/portfolio/update-holding")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }


    //for Sell
    public HoldingResponse getHolding(Long userId, String stockSymbol) {

        return restClient.get()
                .uri(
                        "/api/portfolio/{userId}/holdings/{stockSymbol}",
                        userId,
                        stockSymbol
                )
                .retrieve()
                .body(HoldingResponse.class);
    }
}
