package com.amortix.prime.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * High-Precision Financial Math Engine for Cooperative Banking.
 * Faithfully mirrors legacy stored procedure amortix.SP_Calculations with exact precision.
 */
public final class FinancialMath {

    private FinancialMath() {}

    /**
     * Calculates Equated Monthly Installment (EMI) on Reducing Balance.
     * Formula: E = [P * r * (1 + r)^n] / [(1 + r)^n - 1]
     */
    public static BigDecimal calculateReducingBalanceEmi(BigDecimal principal, double annualRate, int tenureMonths) {
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0 || tenureMonths <= 0) {
            return BigDecimal.ZERO;
        }
        if (annualRate <= 0.0) {
            return principal.divide(BigDecimal.valueOf(tenureMonths), 2, RoundingMode.HALF_UP);
        }

        double monthlyRate = (annualRate / 12.0) / 100.0;
        double factor = Math.pow(1.0 + monthlyRate, tenureMonths);
        double emi = principal.doubleValue() * (monthlyRate * factor) / (factor - 1.0);

        return BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates Fixed Deposit (FXD) Maturity Amount.
     * Supports Quarterly compounding (Standard Indian Cooperative Banking & legacy SP_Calculations).
     * Formula: A = P * (1 + r / (4 * 100)) ^ (4 * (tenureMonths / 12))
     */
    public static BigDecimal calculateFixedDepositMaturity(BigDecimal principal, double annualRate, int tenureMonths, String compounding) {
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0 || tenureMonths <= 0) {
            return BigDecimal.ZERO;
        }
        if (annualRate <= 0.0) {
            return principal;
        }

        if ("SIMPLE".equalsIgnoreCase(compounding)) {
            // A = P + (P * r * (months/12)) / 100
            BigDecimal interest = principal.multiply(BigDecimal.valueOf(annualRate))
                    .multiply(BigDecimal.valueOf(tenureMonths))
                    .divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP);
            return principal.add(interest);
        }

        // Standard Quarterly Compounding
        int frequency = "MONTHLY".equalsIgnoreCase(compounding) ? 12 :
                       "HALF_YEARLY".equalsIgnoreCase(compounding) ? 2 :
                       "YEARLY".equalsIgnoreCase(compounding) ? 1 : 4; // default Quarterly

        double ratePerPeriod = (annualRate / (double) frequency) / 100.0;
        double totalPeriods = (double) frequency * ((double) tenureMonths / 12.0);
        double maturity = principal.doubleValue() * Math.pow(1.0 + ratePerPeriod, totalPeriods);

        return BigDecimal.valueOf(maturity).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates Recurring Cumulative Deposit (RCD) Maturity Amount.
     * Monthly deposits compounded quarterly.
     */
    public static BigDecimal calculateRcdMaturity(BigDecimal monthlyInstallment, double annualRate, int tenureMonths) {
        if (monthlyInstallment == null || monthlyInstallment.compareTo(BigDecimal.ZERO) <= 0 || tenureMonths <= 0) {
            return BigDecimal.ZERO;
        }

        double ratePerQuarter = (annualRate / 4.0) / 100.0;
        double maturity = 0.0;

        for (int m = 1; m <= tenureMonths; m++) {
            // Number of months this installment earns interest
            int monthsRemaining = tenureMonths - m + 1;
            double quarters = monthsRemaining / 3.0;
            maturity += monthlyInstallment.doubleValue() * Math.pow(1.0 + ratePerQuarter, quarters);
        }

        return BigDecimal.valueOf(maturity).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates Maturity Date adhering to Legacy Society cutoff rule:
     * Legacy rule from SP_Calculations:
     * If deposit day <= 3: maturity is on the same day in month + Duration.
     * If deposit day > 3: maturity day becomes 1st of month + Duration + 1.
     */
    public static LocalDate calculateDepositMaturityDate(LocalDate depositDate, int tenureMonths) {
        if (depositDate == null || tenureMonths <= 0) {
            return LocalDate.now().plusMonths(12);
        }

        int day = depositDate.getDayOfMonth();
        if (day <= 3) {
            return depositDate.plusMonths(tenureMonths);
        } else {
            return depositDate.plusMonths(tenureMonths + 1).withDayOfMonth(1);
        }
    }
}
