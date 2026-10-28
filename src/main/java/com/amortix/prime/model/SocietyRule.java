package com.amortix.prime.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Society Master Business Rule Entity.
 * Corresponds to Amortix Rules.
 */
@Entity
@Table(name = "society_rules")
public class SocietyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String ruleCode; // e.g. "MAX_LOAN_MULTIPLIER", "MAX_SURETIES_PER_MEMBER", "MIN_SERVICE_MONTHS"

    private String ruleName;
    private String ruleCategory; // LOAN, DEPOSIT, MEMBERSHIP, THRIFT
    private String ruleValue; // Stored as string for flexibility (e.g. "10", "3", "500000")
    @Column(length = 1000)
    private String description;

    private boolean active = true;
    private LocalDateTime updatedDate = LocalDateTime.now();

    public SocietyRule() {}

    public SocietyRule(String ruleCode, String ruleName, String ruleCategory, String ruleValue, String description) {
        this.ruleCode = ruleCode;
        this.ruleName = ruleName;
        this.ruleCategory = ruleCategory;
        this.ruleValue = ruleValue;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getRuleCategory() { return ruleCategory; }
    public void setRuleCategory(String ruleCategory) { this.ruleCategory = ruleCategory; }
    public String getRuleValue() { return ruleValue; }
    public void setRuleValue(String ruleValue) { this.ruleValue = ruleValue; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}
