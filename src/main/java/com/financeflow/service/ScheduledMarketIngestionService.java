package com.financeflow.service;

import com.financeflow.entity.MarketRate;
import com.financeflow.repository.MarketRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Service
@ConditionalOnProperty(name = "financeflow.ingestion.enabled", havingValue = "true", matchIfMissing = true)
public class ScheduledMarketIngestionService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledMarketIngestionService.class);
    private final MarketRateRepository marketRateRepository;
    private final Random random = new Random();

    public ScheduledMarketIngestionService(MarketRateRepository marketRateRepository) {
        this.marketRateRepository = marketRateRepository;
    }

    // Runs automatically every 60 minutes
    @Scheduled(fixedRate = 3600000)
    public void syncMarketRates() {
        log.info("Executing scheduled exchange rates ingestion pipeline...");

        // Simulated benchmark rates feed ingestion
        double usdInrBase = 83.50 + (random.nextDouble() * 0.40 - 0.20);
        BigDecimal rate = BigDecimal.valueOf(usdInrBase).setScale(4, RoundingMode.HALF_UP);

        MarketRate marketRate = new MarketRate("USD", "INR", rate);
        marketRateRepository.save(marketRate);

        log.info("Market rate benchmark updated: 1 USD = {} INR", rate);
    }
}
