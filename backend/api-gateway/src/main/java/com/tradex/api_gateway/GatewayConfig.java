package com.tradex.api_gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {
    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder,
            @Value("${AUTH_SERVICE_URL:http://localhost:8081}") String auth,
            @Value("${STOCK_SERVICE_URL:http://localhost:8083}") String stock,
            @Value("${WALLET_SERVICE_URL:http://localhost:8084}") String wallet,
            @Value("${TRADING_SERVICE_URL:http://localhost:8085}") String trading,
            @Value("${PORTFOLIO_SERVICE_URL:http://localhost:8086}") String portfolio) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/auth/**").uri(auth))
                .route("stock-service", r -> r.path("/api/stocks/**", "/api/stocks").uri(stock))
                .route("wallet-service", r -> r.path("/api/wallet/**").uri(wallet))
                .route("trading-service", r -> r.path("/api/orders/**").uri(trading))
                .route("portfolio-service", r -> r.path("/api/portfolio/**").uri(portfolio))
                .build();
    }
}
