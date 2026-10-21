package com.amortix.prime.repository;

import com.amortix.prime.model.SocietyRule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocietyRuleRepository extends MongoRepository<SocietyRule, String> {
    Optional<SocietyRule> findByRuleCode(String ruleCode);
    List<SocietyRule> findByRuleCategory(String category);
    List<SocietyRule> findByActiveTrue();
}
