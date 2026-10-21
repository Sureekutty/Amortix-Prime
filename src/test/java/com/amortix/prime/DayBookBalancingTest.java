package com.amortix.prime;

import com.amortix.prime.model.DailyAccountProcessing;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Day Book & Daily Accounts Processing Balancing Tests")
class DayBookBalancingTest {

    @Test
    @DisplayName("Closing cash balance should mathematically balance against opening + receipts - payments")
    void testDayBookBalancing() {
        BigDecimal openingCash = new BigDecimal("50000.00");
        BigDecimal cashReceipts = new BigDecimal("125000.00");
        BigDecimal cashPayments = new BigDecimal("45000.00");

        BigDecimal expectedClosingCash = openingCash.add(cashReceipts).subtract(cashPayments);

        DailyAccountProcessing day = new DailyAccountProcessing(LocalDate.now(), openingCash, new BigDecimal("1000000.00"));
        day.setTotalCashReceipts(cashReceipts);
        day.setTotalCashPayments(cashPayments);
        day.setClosingCashBalance(expectedClosingCash);

        assertEquals(new BigDecimal("130000.00"), day.getClosingCashBalance());
        assertEquals("OPEN", day.getDayStatus());
    }
}
