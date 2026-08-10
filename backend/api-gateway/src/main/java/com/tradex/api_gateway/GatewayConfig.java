package com.tradex.api_gateway;


import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder){
        return builder.routes()
                .route("auth-service",route ->route
                        .path("/api/auth/**")
                        .uri("http://localhost:8081")
                )

                .route("stock-service", route -> route
                        .path("/api/stocks/**")
                        .uri("http://localhost:8083")
                )
//
                // Wallet Service Route - future service
                .route("wallet-service", route -> route
                        .path("/api/wallet/**")
                        .uri("http://localhost:8084")
                )
//
                // Trading Service Route - future service
                .route("trading-service", route -> route
                        .path("/api/orders/**")
                        .uri("http://localhost:8085")
                )
//
                .route("portfolio-service", route -> route
                        .path("/api/portfolio/**")
                        .uri("http://localhost:8086")
                )

                .build();



    }
}
