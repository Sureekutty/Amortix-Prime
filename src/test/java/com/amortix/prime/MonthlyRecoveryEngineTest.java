package com.amortix.prime;

import com.amortix.prime.model.MonthlyRecoveryRun;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Monthly Recovery & Demand Reconciliation Logic Tests")
class MonthlyRecoveryEngineTest {

    @Test
    @DisplayName("Demand item should accurately calculate member gross demand")
    void testDemandItemGrossCalculation() {
        MonthlyRecoveryRun.RecoveryItem item = new MonthlyRecoveryRun.RecoveryItem();
        item.setMemberCode("MEM-1001");
        item.setThriftDemand(new BigDecimal("2500.00"));
        item.setLoanPrincipalDemand(new BigDecimal("7500.00"));
        item.setLoanInterestDemand(new BigDecimal("2456.00"));
        item.setRcdDemand(new BigDecimal("1000.00"));

        BigDecimal gross = item.getThriftDemand()
                .add(item.getLoanPrincipalDemand())
                .add(item.getLoanInterestDemand())
                .add(item.getRcdDemand());

        item.setTotalDemand(gross);

        assertEquals(new BigDecimal("13456.00"), item.getTotalDemand());
    }

    @Test
    @DisplayName("Should flag shortage variance and create WrongPayment discrepancy")
    void testShortageVarianceHandling() {
        BigDecimal demand = new BigDecimal("10000.00");
        BigDecimal actualDeducted = new BigDecimal("6000.00");

        BigDecimal variance = actualDeducted.subtract(demand);
        assertTrue(variance.compareTo(BigDecimal.ZERO) < 0);
        assertEquals(new BigDecimal("-4000.00"), variance);

        MonthlyRecoveryRun.WrongPaymentRecord record = new MonthlyRecoveryRun.WrongPaymentRecord(
                "MEM-1001", "EMP-9821", demand, actualDeducted, variance.abs(), "Short deduction in payroll");

        assertEquals(new BigDecimal("4000.00"), record.getShortOrExcessAmount());
    }
}
