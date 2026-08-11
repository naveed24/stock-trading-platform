package com.tradex.trading_service.client;


import com.tradex.trading_service.dto.WalletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WalletClient {

    private final RestClient restClient;

    public WalletClient(
            @Value("${services.wallet.url}") String walletServiceUrl
    ) {

        this.restClient = RestClient.builder()
                .baseUrl(walletServiceUrl)
                .build();
    }

    public void blockAmount(WalletRequest request) {

        restClient.post()
                .uri("/api/wallet/block")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public void debitAmount(WalletRequest request) {

        restClient.post()
                .uri("/api/wallet/debit")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public void releaseAmount(WalletRequest request) {

        restClient.post()
                .uri("/api/wallet/release")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public void creditAmount(WalletRequest request) {

        restClient.post()
                .uri("/api/wallet/credit")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
