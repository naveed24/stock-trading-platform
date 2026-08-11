package com.tradex.trading_service.client;


import com.tradex.trading_service.dto.StockResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class StockClient {

    private final RestClient restClient;


    public StockClient(
            @Value("${services.stock.url}") String stockServiceUrl
    ) {

        this.restClient = RestClient.builder()
                .baseUrl(stockServiceUrl)
                .build();
    }

    public StockResponse getStock(String symbol) {

        return restClient.get()
                .uri("/api/stocks/{symbol}", symbol)
                .retrieve()
                .body(StockResponse.class);
    }
}
