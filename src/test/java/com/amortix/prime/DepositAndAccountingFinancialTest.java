package com.amortix.prime;

import com.amortix.prime.model.*;
import com.amortix.prime.repository.*;
import com.amortix.prime.service.AccountingService;
import com.amortix.prime.service.DepositService;
import com.amortix.prime.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DepositAndAccountingFinancialTest {

    @Autowired
    private DepositService depositService;

    @Autowired
    private AccountingService accountingService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private GeneralLedgerAccountRepository ledgerRepo;

    @Autowired
    private DailyAccountProcessingRepository dailyRepo;

    @Test
    @DisplayName("Premature Deposit Closure: Elapsed months calculation prevents paying full-term interest")
    void prematureClose_elapsedMonths_correctInterest() {
        // FXD-10001 was opened 4 months ago for 24 months tenure with ₹100,000 principal
        DepositAccount closed = depositService.prematureCloseDeposit("FXD-10001", "Early medical withdrawal", "staff");

        assertEquals("PREMATURE_CLOSED", closed.getStatus());
        // For 4 months elapsed at reduced rate (7.5% - 1.5% = 6.0% simple interest):
        // ₹100,000 * 6.0% * (4/12) = ₹2,000 interest payout
        // Payout should be around ₹102,000, NOT the 24-month amount ₹112,000+!
        assertTrue(closed.getFinalPayoutAmount().compareTo(new BigDecimal("105000.00")) < 0,
                "Payout must reflect 4 elapsed months (around ₹102,000), was: " + closed.getFinalPayoutAmount());
        assertTrue(closed.getFinalPayoutAmount().compareTo(new BigDecimal("100000.00")) >= 0);
    }

    @Test
    @DisplayName("Double-Entry Ledger Integrity: Debiting Asset increases balance, Crediting Asset decreases balance")
    void accountingLedger_assetDebitCredit_correctBalances() {
        GeneralLedgerAccount cash = ledgerRepo.findByAccountCode("1001").orElseThrow();
        BigDecimal initialCash = cash.getCurrentBalance();

        // 1. Debit Cash (Receipt) -> Asset increases
        Voucher rcp = new Voucher();
        rcp.setVoucherType("RECEIPT");
        rcp.setAmount(new BigDecimal("5000.00"));
        rcp.setDebitAccountCode("1001");
        rcp.setCreditAccountCode("3001"); // Thrift Liability increases
        accountingService.createVoucher(rcp, "officer");

        GeneralLedgerAccount updatedCash = ledgerRepo.findByAccountCode("1001").orElseThrow();
        assertEquals(initialCash.add(new BigDecimal("5000.00")), updatedCash.getCurrentBalance());

        // 2. Credit Cash (Payment) -> Asset decreases
        Voucher pmt = new Voucher();
        pmt.setVoucherType("PAYMENT");
        pmt.setAmount(new BigDecimal("2000.00"));
        pmt.setDebitAccountCode("6002"); // Expense increases
        pmt.setCreditAccountCode("1001"); // Cash decreases
        accountingService.createVoucher(pmt, "officer");

        GeneralLedgerAccount finalCash = ledgerRepo.findByAccountCode("1001").orElseThrow();
        assertEquals(initialCash.add(new BigDecimal("3000.00")), finalCash.getCurrentBalance());
    }

    @Test
    @DisplayName("Accounting DayBook Security: Cannot post voucher into a closed accounting day")
    void voucherPosting_closedDayBook_throwsException() {
        LocalDate closedDate = LocalDate.now().minusDays(5);
        DailyAccountProcessing day = accountingService.getOrCreateDayBook(closedDate);
        accountingService.closeAndLockDayBook(closedDate, "Audited and closed", "admin");

        Voucher v = new Voucher();
        v.setVoucherDate(closedDate);
        v.setVoucherType("PAYMENT");
        v.setAmount(new BigDecimal("1000.00"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> accountingService.createVoucher(v, "officer"));
        assertTrue(ex.getMessage().contains("Cannot post voucher into a closed and locked accounting day"));
    }

    @Test
    @DisplayName("Member Settlement Security: Cannot execute payout if outstanding loans exceed savings")
    void memberSettlement_negativeNetPayable_throwsException() {
        // MEM-1001 has Loan LTL-10001 with ₹350,000 outstanding, but only ₹85,000 thrift + ₹5,000 shares
        // Net payable is heavily negative (-₹260,000 deficit)
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> memberService.calculateAndExecuteSettlement("MEM-1001", "Final exit", "admin"));
        assertTrue(ex.getMessage().contains("Cannot execute settlement payout"));
        assertTrue(ex.getMessage().contains("Net deficit"));
    }
}
