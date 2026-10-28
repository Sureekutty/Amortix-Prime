package com.amortix.prime.repository;

import com.amortix.prime.model.InterestRateRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterestRateRuleRepository extends JpaRepository<InterestRateRule, String> {
    List<InterestRateRule> findByProductCodeAndActiveTrue(String productCode);
    List<InterestRateRule> findByProductType(String productType);
    Optional<InterestRateRule> findFirstByProductCodeAndActiveTrueOrderByEffectiveFromDateDesc(String productCode);
}
