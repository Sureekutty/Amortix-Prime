package com.amortix.prime.repository;

import com.amortix.prime.model.SocietyRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocietyRuleRepository extends JpaRepository<SocietyRule, String> {
    Optional<SocietyRule> findByRuleCode(String ruleCode);
    List<SocietyRule> findByRuleCategory(String category);
    List<SocietyRule> findByActiveTrue();
}
