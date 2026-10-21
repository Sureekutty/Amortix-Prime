package com.amortix.prime.service.impl;

import com.amortix.prime.model.*;
import com.amortix.prime.repository.*;
import com.amortix.prime.service.DepositService;
import com.amortix.prime.util.FinancialMath;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DepositServiceImpl implements DepositService {

    private final DepositAccountRepository depositAccountRepository;
    private final DepositTransactionRepository depositTransactionRepository;
    private final MemberRepository memberRepository;
    private final InterestRateRuleRepository interestRateRuleRepository;
    private final VoucherRepository voucherRepository;
    private final AuditLogRepository auditLogRepository;

    public DepositServiceImpl(DepositAccountRepository depositAccountRepository,
                              DepositTransactionRepository depositTransactionRepository,
                              MemberRepository memberRepository,
                              InterestRateRuleRepository interestRateRuleRepository,
                              VoucherRepository voucherRepository,
                              AuditLogRepository auditLogRepository) {
        this.depositAccountRepository = depositAccountRepository;
        this.depositTransactionRepository = depositTransactionRepository;
        this.memberRepository = memberRepository;
        this.interestRateRuleRepository = interestRateRuleRepository;
        this.voucherRepository = voucherRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public DepositAccount openDeposit(DepositAccount deposit, String operatorUser) {
        Member member = memberRepository.findByMemberCode(deposit.getMemberCode())
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + deposit.getMemberCode()));

        deposit.setMemberName(member.getFullName());

        // Dynamic Interest Rate lookup if not specified
        if (deposit.getInterestRate() == null || deposit.getInterestRate() <= 0.0) {
            InterestRateRule rule = interestRateRuleRepository
                    .findFirstByProductCodeAndActiveTrueOrderByEffectiveFromDateDesc(deposit.getDepositType())
                    .orElse(null);
            deposit.setInterestRate(rule != null ? rule.getInterestRate() : 7.5);
        }

        // Generate Account Number
        if (deposit.getDepositAccountNo() == null || deposit.getDepositAccountNo().isBlank()) {
            long count = depositAccountRepository.count() + 10001;
            deposit.setDepositAccountNo(deposit.getDepositType() + "-" + count);
        }

        deposit.setDepositDate(deposit.getDepositDate() != null ? deposit.getDepositDate() : LocalDate.now());

        // Calculate Maturity Date adhering to Legacy Cutoff Rule
        LocalDate maturityDate = FinancialMath.calculateDepositMaturityDate(deposit.getDepositDate(), deposit.getTenureMonths());
        deposit.setMaturityDate(maturityDate);

        // Calculate Maturity Amount
        if ("RCD".equalsIgnoreCase(deposit.getDepositType())) {
            BigDecimal maturity = FinancialMath.calculateRcdMaturity(deposit.getMonthlyInstallment(),
                    deposit.getInterestRate(), deposit.getTenureMonths());
            deposit.setMaturityAmount(maturity);
            deposit.setTotalPaidIn(deposit.getMonthlyInstallment());
            deposit.setPrincipalAmount(deposit.getMonthlyInstallment().multiply(BigDecimal.valueOf(deposit.getTenureMonths())));
        } else {
            // FXD or MIS
            BigDecimal maturity = FinancialMath.calculateFixedDepositMaturity(deposit.getPrincipalAmount(),
                    deposit.getInterestRate(), deposit.getTenureMonths(), deposit.getCompoundingFrequency());
            deposit.setMaturityAmount(maturity);
            deposit.setTotalPaidIn(deposit.getPrincipalAmount());
        }

        deposit.setAccumulatedInterest(deposit.getMaturityAmount().subtract(deposit.getTotalPaidIn()).max(BigDecimal.ZERO));
        deposit.setStatus("ACTIVE");

        DepositAccount saved = depositAccountRepository.save(deposit);

        // Record Initial Opening Transaction
        DepositTransaction txn = new DepositTransaction();
        txn.setDepositAccountNo(saved.getDepositAccountNo());
        txn.setMemberCode(saved.getMemberCode());
        txn.setTxnDate(saved.getDepositDate());
        txn.setTxnType("OPENING");
        txn.setAmount(saved.getTotalPaidIn());
        txn.setBalanceAfterTxn(saved.getTotalPaidIn());
        txn.setRemarks("Deposit account opened");
        txn.setCreatedBy(operatorUser);
        depositTransactionRepository.save(txn);

        // Record Receipt Voucher
        Voucher rcp = new Voucher();
        rcp.setVoucherNo("DEP-" + saved.getDepositAccountNo());
        rcp.setVoucherType("RECEIPT");
        rcp.setMemberCode(saved.getMemberCode());
        rcp.setMemberName(saved.getMemberName());
        rcp.setAmount(saved.getTotalPaidIn());
        rcp.setCategory("DEPOSIT_OPENING");
        rcp.setNarration("Opening of " + saved.getDepositType() + " account " + saved.getDepositAccountNo());
        rcp.setPreparedBy(operatorUser);
        voucherRepository.save(rcp);

        auditLogRepository.save(new AuditLog(operatorUser, "DEPOSIT_OPENED", "Deposit", saved.getDepositAccountNo(),
                "Opened " + saved.getDepositType() + " for " + saved.getMemberCode() + ", Amount: " + saved.getTotalPaidIn()));

        return saved;
    }

    @Override
    @Transactional
    public DepositAccount prematureCloseDeposit(String depositAccountNo, String remarks, String operatorUser) {
        DepositAccount dep = findByDepositAccountNo(depositAccountNo);
        if ("PREMATURE_CLOSED".equals(dep.getStatus()) || "MATURED".equals(dep.getStatus())) {
            throw new IllegalStateException("Deposit is already closed/matured: " + dep.getStatus());
        }
        if (dep.isLienMarked()) {
            throw new IllegalStateException("Cannot close deposit pledged as collateral for active loan: " + dep.getLienLoanAccountNo());
        }

        // Fetch Penalty Rate from Interest Rule (FXP)
        double penaltyRate = 1.5; // default 1.5% penalty for premature withdrawal
        InterestRateRule penaltyRule = interestRateRuleRepository
                .findFirstByProductCodeAndActiveTrueOrderByEffectiveFromDateDesc("FXP")
                .orElse(null);
        if (penaltyRule != null) {
            penaltyRate = penaltyRule.getInterestRate();
        }

        double reducedRate = Math.max(0.5, dep.getInterestRate() - penaltyRate);
        dep.setPrematurePenaltyRate(BigDecimal.valueOf(penaltyRate));

        // Recompute reduced interest payout
        BigDecimal reducedMaturity = FinancialMath.calculateFixedDepositMaturity(dep.getPrincipalAmount(),
                reducedRate, dep.getTenureMonths(), "SIMPLE");
        dep.setFinalPayoutAmount(reducedMaturity);
        dep.setStatus("PREMATURE_CLOSED");
        dep.setClosureDate(LocalDate.now());
        dep.setRemarks("Premature closure: " + remarks);

        DepositAccount closed = depositAccountRepository.save(dep);

        // Record Transaction
        DepositTransaction txn = new DepositTransaction();
        txn.setDepositAccountNo(dep.getDepositAccountNo());
        txn.setMemberCode(dep.getMemberCode());
        txn.setTxnDate(LocalDate.now());
        txn.setTxnType("PREMATURE_CLOSURE");
        txn.setAmount(reducedMaturity);
        txn.setBalanceAfterTxn(BigDecimal.ZERO);
        txn.setRemarks("Premature closure with " + penaltyRate + "% penalty. Payout: ₹" + reducedMaturity);
        txn.setCreatedBy(operatorUser);
        depositTransactionRepository.save(txn);

        // Payment voucher
        Voucher pmt = new Voucher();
        pmt.setVoucherNo("DEPCLO-" + dep.getDepositAccountNo());
        pmt.setVoucherType("PAYMENT");
        pmt.setMemberCode(dep.getMemberCode());
        pmt.setMemberName(dep.getMemberName());
        pmt.setAmount(reducedMaturity);
        pmt.setCategory("DEPOSIT_PAYOUT");
        pmt.setNarration("Premature closure payout for " + dep.getDepositAccountNo());
        pmt.setPreparedBy(operatorUser);
        voucherRepository.save(pmt);

        return closed;
    }

    @Override
    @Transactional
    public DepositTransaction recordRcdInstallment(String depositAccountNo, BigDecimal installmentAmount, String receiptNo, String operatorUser) {
        DepositAccount dep = findByDepositAccountNo(depositAccountNo);
        dep.setTotalPaidIn(dep.getTotalPaidIn().add(installmentAmount));
        depositAccountRepository.save(dep);

        DepositTransaction txn = new DepositTransaction();
        txn.setDepositAccountNo(dep.getDepositAccountNo());
        txn.setMemberCode(dep.getMemberCode());
        txn.setTxnType("INSTALLMENT");
        txn.setAmount(installmentAmount);
        txn.setBalanceAfterTxn(dep.getTotalPaidIn());
        txn.setVoucherOrReceiptNo(receiptNo);
        txn.setRemarks("Monthly RCD installment collected");
        txn.setCreatedBy(operatorUser);

        return depositTransactionRepository.save(txn);
    }

    @Override
    public DepositAccount findByDepositAccountNo(String depositAccountNo) {
        return depositAccountRepository.findByDepositAccountNo(depositAccountNo)
                .orElseThrow(() -> new IllegalArgumentException("Deposit account not found: " + depositAccountNo));
    }

    @Override
    public List<DepositAccount> findByMemberCode(String memberCode) {
        return depositAccountRepository.findByMemberCode(memberCode);
    }

    @Override
    public Page<DepositAccount> listDeposits(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return depositAccountRepository.findByDepositAccountNoContainingIgnoreCaseOrMemberCodeContainingIgnoreCaseOrMemberNameContainingIgnoreCase(
                    search, search, search, pageable);
        }
        return depositAccountRepository.findAll(pageable);
    }

    @Override
    public List<DepositTransaction> getDepositTransactions(String depositAccountNo) {
        return depositTransactionRepository.findByDepositAccountNoOrderByTxnDateDesc(depositAccountNo);
    }

    @Override
    public MaturityPreview previewMaturity(String depositType, BigDecimal amount, int tenureMonths, Double customRate) {
        double rate = (customRate != null && customRate > 0.0) ? customRate : 7.5;
        BigDecimal maturity;
        if ("RCD".equalsIgnoreCase(depositType)) {
            maturity = FinancialMath.calculateRcdMaturity(amount, rate, tenureMonths);
        } else {
            maturity = FinancialMath.calculateFixedDepositMaturity(amount, rate, tenureMonths, "QUARTERLY");
        }
        LocalDate matDate = FinancialMath.calculateDepositMaturityDate(LocalDate.now(), tenureMonths);
        return new MaturityPreview(amount, rate, tenureMonths, maturity, matDate.toString());
    }
}
