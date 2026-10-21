package com.amortix.prime.service.impl;

import com.amortix.prime.model.AuditLog;
import com.amortix.prime.model.DailyAccountProcessing;
import com.amortix.prime.model.GeneralLedgerAccount;
import com.amortix.prime.model.Voucher;
import com.amortix.prime.repository.AuditLogRepository;
import com.amortix.prime.repository.DailyAccountProcessingRepository;
import com.amortix.prime.repository.GeneralLedgerAccountRepository;
import com.amortix.prime.repository.VoucherRepository;
import com.amortix.prime.service.AccountingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AccountingServiceImpl implements AccountingService {

    private final DailyAccountProcessingRepository dailyRepo;
    private final VoucherRepository voucherRepo;
    private final GeneralLedgerAccountRepository ledgerRepo;
    private final AuditLogRepository auditLogRepo;

    public AccountingServiceImpl(DailyAccountProcessingRepository dailyRepo,
                                 VoucherRepository voucherRepo,
                                 GeneralLedgerAccountRepository ledgerRepo,
                                 AuditLogRepository auditLogRepo) {
        this.dailyRepo = dailyRepo;
        this.voucherRepo = voucherRepo;
        this.ledgerRepo = ledgerRepo;
        this.auditLogRepo = auditLogRepo;
    }

    @Override
    @Transactional
    public DailyAccountProcessing getOrCreateDayBook(LocalDate date) {
        return dailyRepo.findByProcessingDate(date).orElseGet(() -> {
            // Fetch yesterday's closing to use as today's opening
            DailyAccountProcessing last = dailyRepo.findTopByOrderByProcessingDateDesc().orElse(null);
            BigDecimal openCash = last != null ? last.getClosingCashBalance() : new BigDecimal("50000.00");
            BigDecimal openBank = last != null ? last.getClosingBankBalance() : new BigDecimal("2500000.00");

            DailyAccountProcessing newDay = new DailyAccountProcessing(date, openCash, openBank);
            return dailyRepo.save(newDay);
        });
    }

    @Override
    @Transactional
    public DailyAccountProcessing closeAndLockDayBook(LocalDate date, String remarks, String operatorUser) {
        DailyAccountProcessing day = getOrCreateDayBook(date);
        DayBookSummary summary = generateDayBookSummary(date);

        day.setTotalCashReceipts(summary.totalCashReceipts());
        day.setTotalBankReceipts(summary.totalBankReceipts());
        day.setTotalCashPayments(summary.totalCashPayments());
        day.setTotalBankPayments(summary.totalBankPayments());
        day.setClosingCashBalance(summary.closingCash());
        day.setClosingBankBalance(summary.closingBank());
        day.setDayStatus("CLOSED");
        day.setClosedBy(operatorUser);
        day.setClosedTime(LocalDateTime.now());
        day.setRemarks(remarks);

        DailyAccountProcessing saved = dailyRepo.save(day);
        auditLogRepo.save(new AuditLog(operatorUser, "DAY_BOOK_CLOSED", "DailyAccountsProcessing", date.toString(),
                "Closed day book for " + date + ". Closing Cash: ₹" + saved.getClosingCashBalance()));
        return saved;
    }

    @Override
    @Transactional
    public Voucher createVoucher(Voucher voucher, String operatorUser) {
        if (voucher.getVoucherNo() == null || voucher.getVoucherNo().isBlank()) {
            String prefix = "RECEIPT".equalsIgnoreCase(voucher.getVoucherType()) ? "RCP" :
                           "PAYMENT".equalsIgnoreCase(voucher.getVoucherType()) ? "PMT" :
                           "JOURNAL_VOUCHER".equalsIgnoreCase(voucher.getVoucherType()) ? "JV" : "MISC";
            voucher.setVoucherNo(prefix + "-" + System.currentTimeMillis() % 1000000);
        }
        voucher.setPreparedBy(operatorUser);
        voucher.setStatus("POSTED");
        Voucher saved = voucherRepo.save(voucher);

        // Update ledger balances if accounts are provided
        if (voucher.getDebitAccountCode() != null) {
            ledgerRepo.findByAccountCode(voucher.getDebitAccountCode()).ifPresent(acc -> {
                acc.setCurrentBalance(acc.getCurrentBalance().add(voucher.getAmount()));
                ledgerRepo.save(acc);
            });
        }
        if (voucher.getCreditAccountCode() != null) {
            ledgerRepo.findByAccountCode(voucher.getCreditAccountCode()).ifPresent(acc -> {
                acc.setCurrentBalance(acc.getCurrentBalance().add(voucher.getAmount()));
                ledgerRepo.save(acc);
            });
        }

        auditLogRepo.save(new AuditLog(operatorUser, "VOUCHER_CREATED", "Voucher", saved.getVoucherNo(),
                "Created " + saved.getVoucherType() + " of ₹" + saved.getAmount()));
        return saved;
    }

    @Override
    public Voucher findByVoucherNo(String voucherNo) {
        return voucherRepo.findByVoucherNo(voucherNo)
                .orElseThrow(() -> new IllegalArgumentException("Voucher not found: " + voucherNo));
    }

    @Override
    public Page<Voucher> listVouchers(String type, Pageable pageable) {
        if (type != null && !type.isBlank()) {
            return voucherRepo.findByVoucherType(type, pageable);
        }
        return voucherRepo.findAllByOrderByVoucherDateDesc(pageable);
    }

    @Override
    public List<Voucher> getDayBookVouchers(LocalDate date) {
        return voucherRepo.findByVoucherDate(date);
    }

    @Override
    public DayBookSummary generateDayBookSummary(LocalDate date) {
        DailyAccountProcessing day = getOrCreateDayBook(date);
        List<Voucher> vouchers = voucherRepo.findByVoucherDate(date);

        BigDecimal cashReceipts = BigDecimal.ZERO;
        BigDecimal bankReceipts = BigDecimal.ZERO;
        BigDecimal cashPayments = BigDecimal.ZERO;
        BigDecimal bankPayments = BigDecimal.ZERO;

        for (Voucher v : vouchers) {
            boolean isCash = "CASH".equalsIgnoreCase(v.getPaymentMode());
            if ("RECEIPT".equalsIgnoreCase(v.getVoucherType())) {
                if (isCash) cashReceipts = cashReceipts.add(v.getAmount());
                else bankReceipts = bankReceipts.add(v.getAmount());
            } else if ("PAYMENT".equalsIgnoreCase(v.getVoucherType()) || "MISC_EXPENSE".equalsIgnoreCase(v.getVoucherType())) {
                if (isCash) cashPayments = cashPayments.add(v.getAmount());
                else bankPayments = bankPayments.add(v.getAmount());
            }
        }

        BigDecimal closingCash = day.getOpeningCashBalance().add(cashReceipts).subtract(cashPayments);
        BigDecimal closingBank = day.getOpeningBankBalance().add(bankReceipts).subtract(bankPayments);

        return new DayBookSummary(date, day.getOpeningCashBalance(), day.getOpeningBankBalance(),
                cashReceipts, bankReceipts, cashPayments, bankPayments, closingCash, closingBank,
                day.getDayStatus(), vouchers);
    }

    @Override
    public List<GeneralLedgerAccount> getChartOfAccounts() {
        return ledgerRepo.findByActiveTrueOrderByAccountCodeAsc();
    }

    @Override
    public TrialBalanceReport generateTrialBalance() {
        List<GeneralLedgerAccount> accounts = ledgerRepo.findByActiveTrueOrderByAccountCodeAsc();
        BigDecimal totalDebits = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        List<TrialBalanceReport.LedgerItem> items = new ArrayList<>();
        for (GeneralLedgerAccount acc : accounts) {
            BigDecimal deb = BigDecimal.ZERO;
            BigDecimal cre = BigDecimal.ZERO;

            if ("DEBIT".equalsIgnoreCase(acc.getNormalBalance())) {
                deb = acc.getCurrentBalance();
                totalDebits = totalDebits.add(deb);
            } else {
                cre = acc.getCurrentBalance();
                totalCredits = totalCredits.add(cre);
            }
            items.add(new TrialBalanceReport.LedgerItem(acc.getAccountCode(), acc.getAccountName(),
                    acc.getAccountType(), deb, cre));
        }

        boolean balanced = totalDebits.compareTo(totalCredits) == 0;
        return new TrialBalanceReport(LocalDate.now(), totalDebits, totalCredits, balanced, items);
    }
}
