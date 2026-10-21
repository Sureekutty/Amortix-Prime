package com.amortix.prime.service;

import com.amortix.prime.model.DailyAccountProcessing;
import com.amortix.prime.model.GeneralLedgerAccount;
import com.amortix.prime.model.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface AccountingService {
    DailyAccountProcessing getOrCreateDayBook(LocalDate date);
    DailyAccountProcessing closeAndLockDayBook(LocalDate date, String remarks, String operatorUser);

    Voucher createVoucher(Voucher voucher, String operatorUser);
    Voucher findByVoucherNo(String voucherNo);
    Page<Voucher> listVouchers(String type, Pageable pageable);
    List<Voucher> getDayBookVouchers(LocalDate date);

    DayBookSummary generateDayBookSummary(LocalDate date);
    List<GeneralLedgerAccount> getChartOfAccounts();
    TrialBalanceReport generateTrialBalance();

    record DayBookSummary(
            LocalDate date,
            BigDecimal openingCash,
            BigDecimal openingBank,
            BigDecimal totalCashReceipts,
            BigDecimal totalBankReceipts,
            BigDecimal totalCashPayments,
            BigDecimal totalBankPayments,
            BigDecimal closingCash,
            BigDecimal closingBank,
            String dayStatus,
            List<Voucher> vouchers
    ) {}

    record TrialBalanceReport(
            LocalDate asOfDate,
            BigDecimal totalDebits,
            BigDecimal totalCredits,
            boolean isBalanced,
            List<LedgerItem> items
    ) {
        public record LedgerItem(String code, String name, String type, BigDecimal debitBalance, BigDecimal creditBalance) {}
    }
}
