package com.tradex.stock_service;

import com.tradex.stock_service.entity.Stock;
import com.tradex.stock_service.repository.StockRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class StockDemoSeeder implements ApplicationRunner {
    private final StockRepository repository;
    private final boolean enabled;

    public StockDemoSeeder(StockRepository repository,
            @Value("${tradex.seed-demo:false}") boolean enabled) {
        this.repository = repository;
        this.enabled = enabled;
    }

    @Override public void run(ApplicationArguments args) {
        if (!enabled) return;
        // Fictional and static DEMO prices. No live market feed is connected.
        seed("TCS","Tata Consultancy Services","Technology","3850.00","3832.50");
        seed("INFY","Infosys","Technology","1640.00","1624.00");
        seed("RELIANCE","Reliance Industries","Energy","2980.00","2962.25");
        seed("HDFCBANK","HDFC Bank","Banking","1752.00","1761.00");
        seed("ICICIBANK","ICICI Bank","Banking","1222.00","1210.50");
        seed("WIPRO","Wipro","Technology","532.00","527.50");
        seed("SBIN","State Bank of India","Banking","811.00","818.00");
    }

    private void seed(String symbol, String name, String sector, String current, String close) {
        if (repository.existsBySymbolIgnoreCase(symbol)) return;
        repository.save(Stock.builder()
                .symbol(symbol).companyName(name).sector(sector).exchangeName("NSE")
                .currentPrice(new BigDecimal(current))
                .previousClose(new BigDecimal(close)).active(true).build());
    }
}
