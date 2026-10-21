package com.amortix.prime;

import com.amortix.prime.util.FinancialMath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Cooperative Financial Math Engine Tests")
class FinancialMathTest {

    @Test
    @DisplayName("Should accurately calculate Reducing Balance Loan EMI")
    void testReducingBalanceEmi() {
        // Loan: ₹1,00,000, 12% annual rate, 12 months tenure
        BigDecimal principal = new BigDecimal("100000.00");
        double annualRate = 12.0;
        int tenureMonths = 12;

        BigDecimal emi = FinancialMath.calculateReducingBalanceEmi(principal, annualRate, tenureMonths);

        // Standard banking EMI for 100k at 12% for 12 mos is ~₹8884.88
        assertNotNull(emi);
        assertTrue(emi.compareTo(new BigDecimal("8880.00")) > 0);
        assertTrue(emi.compareTo(new BigDecimal("8890.00")) < 0);
    }

    @Test
    @DisplayName("Should accurately calculate Fixed Deposit (FXD) Quarterly Compounding Maturity")
    void testFixedDepositMaturity() {
        // Deposit: ₹50,000, 8.0% annual rate, 12 months tenure, Quarterly compounding
        BigDecimal principal = new BigDecimal("50000.00");
        double annualRate = 8.0;
        int tenureMonths = 12;

        BigDecimal maturity = FinancialMath.calculateFixedDepositMaturity(principal, annualRate, tenureMonths, "QUARTERLY");

        // Formula: 50000 * (1 + 0.02)^4 = 50000 * 1.082432 = ₹54,121.61
        assertNotNull(maturity);
        assertTrue(maturity.compareTo(new BigDecimal("54120.00")) > 0);
        assertTrue(maturity.compareTo(new BigDecimal("54125.00")) < 0);
    }

    @Test
    @DisplayName("Should accurately calculate Recurring Cumulative Deposit (RCD) Maturity")
    void testRcdMaturity() {
        BigDecimal monthlyInstallment = new BigDecimal("1000.00");
        double annualRate = 7.0;
        int tenureMonths = 12;

        BigDecimal maturity = FinancialMath.calculateRcdMaturity(monthlyInstallment, annualRate, tenureMonths);

        assertNotNull(maturity);
        // Gross deposits = ₹12,000; with compound quarterly interest ~₹12,460+
        assertTrue(maturity.compareTo(new BigDecimal("12400.00")) > 0);
    }

    @Test
    @DisplayName("Should enforce Legacy SP_Calculations Day Cutoff Date Rule (<= 3rd vs 4th+)")
    void testDepositMaturityDateCutoffRule() {
        // Case 1: Deposit booked on 2nd of the month (<= 3rd) -> Matures on 2nd of Month + Tenure
        LocalDate bookedEarly = LocalDate.of(2026, 1, 2);
        LocalDate maturityEarly = FinancialMath.calculateDepositMaturityDate(bookedEarly, 12);
        assertEquals(LocalDate.of(2027, 1, 2), maturityEarly);

        // Case 2: Deposit booked on 15th of the month (> 3rd) -> Rolls over to 1st of Month + Tenure + 1
        LocalDate bookedLate = LocalDate.of(2026, 1, 15);
        LocalDate maturityLate = FinancialMath.calculateDepositMaturityDate(bookedLate, 12);
        assertEquals(LocalDate.of(2027, 2, 1), maturityLate);
    }
}
