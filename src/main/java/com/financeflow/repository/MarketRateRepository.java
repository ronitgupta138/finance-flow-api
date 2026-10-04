package com.financeflow.repository;

import com.financeflow.entity.MarketRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MarketRateRepository extends JpaRepository<MarketRate, Long> {
    Optional<MarketRate> findTopByBaseCurrencyAndTargetCurrencyOrderByFetchedAtDesc(String base, String target);
}
