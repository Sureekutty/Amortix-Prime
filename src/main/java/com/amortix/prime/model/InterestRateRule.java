package com.amortix.prime.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Interest Rate Rule Master Document.
 * Corresponds to legacy speccs.Interest.
 * Configures interest rates for all products: LTL, EXL, ELG, FDL, FXD, RCD, THRIFT.
 */
@Document(collection = "interest_rate_rules")
public class InterestRateRule {

    @Id
    private String id;

    @Indexed
    private String productCode; // LTL, EXL, ELG, FDL, FXD, RCD, THRIFT, FXP (Fixed Deposit Penalty)

    private String productName;
    private String productType; // LOAN, DEPOSIT, SAVINGS
    private int minTenureMonths = 1;
    private int maxTenureMonths = 120;

    private Double interestRate = 8.5; // Annual percentage
    private String calculationMethod = "REDUCING_BALANCE"; // REDUCING_BALANCE, SIMPLE, COMPOUND_QUARTERLY

    private LocalDate effectiveFromDate = LocalDate.now();
    private LocalDate effectiveToDate = LocalDate.of(2099, 12, 31);
    private boolean active = true;

    private LocalDateTime updatedDate = LocalDateTime.now();

    public InterestRateRule() {}

    public InterestRateRule(String productCode, String productName, String productType, int minTenure, int maxTenure, Double rate, String method) {
        this.productCode = productCode;
        this.productName = productName;
        this.productType = productType;
        this.minTenureMonths = minTenure;
        this.maxTenureMonths = maxTenure;
        this.interestRate = rate;
        this.calculationMethod = method;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    public int getMinTenureMonths() { return minTenureMonths; }
    public void setMinTenureMonths(int minTenureMonths) { this.minTenureMonths = minTenureMonths; }
    public int getMaxTenureMonths() { return maxTenureMonths; }
    public void setMaxTenureMonths(int maxTenureMonths) { this.maxTenureMonths = maxTenureMonths; }
    public Double getInterestRate() { return interestRate; }
    public void setInterestRate(Double interestRate) { this.interestRate = interestRate; }
    public String getCalculationMethod() { return calculationMethod; }
    public void setCalculationMethod(String calculationMethod) { this.calculationMethod = calculationMethod; }
    public LocalDate getEffectiveFromDate() { return effectiveFromDate; }
    public void setEffectiveFromDate(LocalDate effectiveFromDate) { this.effectiveFromDate = effectiveFromDate; }
    public LocalDate getEffectiveToDate() { return effectiveToDate; }
    public void setEffectiveToDate(LocalDate effectiveToDate) { this.effectiveToDate = effectiveToDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}
