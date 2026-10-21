package com.amortix.prime.service.impl;

import com.amortix.prime.model.*;
import com.amortix.prime.repository.*;
import com.amortix.prime.service.LoanService;
import com.amortix.prime.util.FinancialMath;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final MemberRepository memberRepository;
    private final SuretyReplacementRepository suretyReplacementRepository;
    private final VoucherRepository voucherRepository;
    private final SocietyRuleRepository societyRuleRepository;
    private final InterestRateRuleRepository interestRateRuleRepository;
    private final AuditLogRepository auditLogRepository;

    public LoanServiceImpl(LoanRepository loanRepository,
                           LoanTransactionRepository loanTransactionRepository,
                           MemberRepository memberRepository,
                           SuretyReplacementRepository suretyReplacementRepository,
                           VoucherRepository voucherRepository,
                           SocietyRuleRepository societyRuleRepository,
                           InterestRateRuleRepository interestRateRuleRepository,
                           AuditLogRepository auditLogRepository) {
        this.loanRepository = loanRepository;
        this.loanTransactionRepository = loanTransactionRepository;
        this.memberRepository = memberRepository;
        this.suretyReplacementRepository = suretyReplacementRepository;
        this.voucherRepository = voucherRepository;
        this.societyRuleRepository = societyRuleRepository;
        this.interestRateRuleRepository = interestRateRuleRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public Loan applyForLoan(Loan loan) {
        Member member = memberRepository.findByMemberCode(loan.getMemberCode())
                .orElseThrow(() -> new IllegalArgumentException("Borrower member not found: " + loan.getMemberCode()));

        // Verification of Basic Pay Multiplier Rule (Legacy Rules table)
        int multiplier = 10;
        SocietyRule multiplierRule = societyRuleRepository.findByRuleCode("MAX_LOAN_MULTIPLIER").orElse(null);
        if (multiplierRule != null) {
            try { multiplier = Integer.parseInt(multiplierRule.getRuleValue()); } catch (Exception ignored) {}
        }
        BigDecimal maxAllowedAmount = member.getBasicPay().multiply(BigDecimal.valueOf(multiplier));
        if (maxAllowedAmount.compareTo(BigDecimal.ZERO) > 0 && loan.getSanctionAmount().compareTo(maxAllowedAmount) > 0) {
            throw new IllegalStateException("Requested loan amount ₹" + loan.getSanctionAmount() +
                    " exceeds maximum allowed limit (10x Basic Pay: ₹" + maxAllowedAmount + ")");
        }

        // Fetch dynamic interest rate from rule master if not set
        if (loan.getInterestRate() == null || loan.getInterestRate() <= 0.0) {
            InterestRateRule rateRule = interestRateRuleRepository
                    .findFirstByProductCodeAndActiveTrueOrderByEffectiveFromDateDesc(loan.getLoanType())
                    .orElse(null);
            loan.setInterestRate(rateRule != null ? rateRule.getInterestRate() : 9.5);
        }

        // Calculate Monthly EMI
        BigDecimal emi = FinancialMath.calculateReducingBalanceEmi(
                loan.getSanctionAmount(), loan.getInterestRate(), loan.getTenureMonths());
        loan.setMonthlyInstallment(emi);

        // Generate Loan Account No if not present
        if (loan.getLoanAccountNo() == null || loan.getLoanAccountNo().isBlank()) {
            long count = loanRepository.count() + 10001;
            loan.setLoanAccountNo(loan.getLoanType() + "-" + count);
        }

        loan.setMemberName(member.getFullName());
        loan.setEmployeeCode(member.getEmployeeCode());
        loan.setDepartment(member.getDepartment());
        loan.setOutstandingPrincipal(loan.getSanctionAmount());
        loan.setOutstandingInterest(BigDecimal.ZERO);
        loan.setStatus("APPLIED");
        loan.setApplicationDate(LocalDate.now());

        Loan saved = loanRepository.save(loan);
        auditLogRepository.save(new AuditLog("SYSTEM", "LOAN_APPLICATION", "Loan", saved.getLoanAccountNo(),
                "Loan application submitted for " + saved.getMemberCode() + ", Amount: " + saved.getSanctionAmount()));
        return saved;
    }

    @Override
    @Transactional
    public Loan approveLoan(String loanAccountNo, String approvedBy) {
        Loan loan = findByLoanAccountNo(loanAccountNo);
        loan.setStatus("APPROVED");
        loan.setSanctionDate(LocalDate.now());
        loan.setSanctionedBy(approvedBy);
        return loanRepository.save(loan);
    }

    @Override
    @Transactional
    public Loan sanctionAndDisburseLoan(String loanAccountNo, String paymentMode, String chequeOrRefNo, String officerUser) {
        Loan loan = findByLoanAccountNo(loanAccountNo);
        loan.setStatus("ACTIVE");
        loan.setDisbursementDate(LocalDate.now());
        loan.setRecoveryStartDate(LocalDate.now().plusMonths(1).withDayOfMonth(1));
        loan.setDisbursementMode(paymentMode);
        loan.setChequeOrRefNo(chequeOrRefNo);
        loan.setSanctionedBy(officerUser);

        // Update active surety count for all guarantors
        if (loan.getSureties() != null) {
            for (Loan.LoanSurety s : loan.getSureties()) {
                if (s.getSuretyMemberCode() != null) {
                    memberRepository.findByMemberCode(s.getSuretyMemberCode()).ifPresent(suretyMember -> {
                        suretyMember.setActiveSuretyCount(suretyMember.getActiveSuretyCount() + 1);
                        memberRepository.save(suretyMember);
                    });
                }
            }
        }

        // Post disbursement transaction
        LoanTransaction txn = new LoanTransaction();
        txn.setLoanAccountNo(loan.getLoanAccountNo());
        txn.setMemberCode(loan.getMemberCode());
        txn.setTxnType("DISBURSEMENT");
        txn.setPrincipalAmount(loan.getSanctionAmount());
        txn.setTotalAmount(loan.getSanctionAmount());
        txn.setBalancePrincipalAfterTxn(loan.getSanctionAmount());
        txn.setPaymentMode(paymentMode);
        txn.setVoucherNo(chequeOrRefNo);
        txn.setRemarks("Loan sanctioned and disbursed to member");
        txn.setCreatedBy(officerUser);
        loanTransactionRepository.save(txn);

        // Create Payment Voucher
        Voucher v = new Voucher();
        v.setVoucherNo("DISB-" + loan.getLoanAccountNo());
        v.setVoucherType("PAYMENT");
        v.setMemberCode(loan.getMemberCode());
        v.setMemberName(loan.getMemberName());
        v.setAmount(loan.getSanctionAmount());
        v.setPaymentMode(paymentMode);
        v.setInstrumentNo(chequeOrRefNo);
        v.setCategory("LOAN_DISBURSEMENT");
        v.setNarration("Disbursement of " + loan.getLoanType() + " account " + loan.getLoanAccountNo());
        v.setPreparedBy(officerUser);
        voucherRepository.save(v);

        Loan updated = loanRepository.save(loan);
        auditLogRepository.save(new AuditLog(officerUser, "LOAN_DISBURSED", "Loan", loan.getLoanAccountNo(),
                "Disbursed loan amount: " + loan.getSanctionAmount()));
        return updated;
    }

    @Override
    @Transactional
    public Loan rejectLoan(String loanAccountNo, String reason, String officerUser) {
        Loan loan = findByLoanAccountNo(loanAccountNo);
        loan.setStatus("REJECTED");
        loan.setRemarks("Rejected: " + reason);
        return loanRepository.save(loan);
    }

    @Override
    @Transactional
    public LoanTransaction recordLoanRepayment(String loanAccountNo, BigDecimal principalAmount, BigDecimal interestAmount,
                                             String paymentMode, String receiptNo, String remarks, String operatorUser) {
        Loan loan = findByLoanAccountNo(loanAccountNo);

        BigDecimal pri = principalAmount != null ? principalAmount : BigDecimal.ZERO;
        BigDecimal intr = interestAmount != null ? interestAmount : BigDecimal.ZERO;
        BigDecimal total = pri.add(intr);

        loan.setOutstandingPrincipal(loan.getOutstandingPrincipal().subtract(pri).max(BigDecimal.ZERO));
        loan.setOutstandingInterest(loan.getOutstandingInterest().subtract(intr).max(BigDecimal.ZERO));
        loan.setTotalPrincipalRepaid(loan.getTotalPrincipalRepaid().add(pri));
        loan.setTotalInterestRepaid(loan.getTotalInterestRepaid().add(intr));

        if (loan.getOutstandingPrincipal().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus("CLOSED");
            loan.setClosedDate(LocalDate.now());

            // Release surety counts
            if (loan.getSureties() != null) {
                for (Loan.LoanSurety s : loan.getSureties()) {
                    memberRepository.findByMemberCode(s.getSuretyMemberCode()).ifPresent(suretyMember -> {
                        suretyMember.setActiveSuretyCount(Math.max(0, suretyMember.getActiveSuretyCount() - 1));
                        memberRepository.save(suretyMember);
                    });
                }
            }
        }
        loanRepository.save(loan);

        LoanTransaction txn = new LoanTransaction();
        txn.setLoanAccountNo(loan.getLoanAccountNo());
        txn.setMemberCode(loan.getMemberCode());
        txn.setTxnType("REPAYMENT");
        txn.setPrincipalAmount(pri);
        txn.setInterestAmount(intr);
        txn.setTotalAmount(total);
        txn.setBalancePrincipalAfterTxn(loan.getOutstandingPrincipal());
        txn.setBalanceInterestAfterTxn(loan.getOutstandingInterest());
        txn.setPaymentMode(paymentMode);
        txn.setReceiptNo(receiptNo);
        txn.setRemarks(remarks);
        txn.setCreatedBy(operatorUser);
        LoanTransaction savedTxn = loanTransactionRepository.save(txn);

        // Record Receipt Voucher
        Voucher rcp = new Voucher();
        rcp.setVoucherNo(receiptNo != null && !receiptNo.isBlank() ? receiptNo : "RCP-" + System.currentTimeMillis() % 100000);
        rcp.setVoucherType("RECEIPT");
        rcp.setMemberCode(loan.getMemberCode());
        rcp.setMemberName(loan.getMemberName());
        rcp.setAmount(total);
        rcp.setPaymentMode(paymentMode);
        rcp.setCategory("LOAN_REPAYMENT");
        rcp.setNarration("Repayment for " + loan.getLoanAccountNo() + " (Principal: " + pri + ", Interest: " + intr + ")");
        rcp.setPreparedBy(operatorUser);
        voucherRepository.save(rcp);

        return savedTxn;
    }

    @Override
    @Transactional
    public SuretyReplacement requestSuretyReplacement(String loanAccountNo, String oldSuretyMemberCode,
                                                     String newSuretyMemberCode, String reason) {
        Loan loan = findByLoanAccountNo(loanAccountNo);
        Member oldSurety = memberRepository.findByMemberCode(oldSuretyMemberCode)
                .orElseThrow(() -> new IllegalArgumentException("Old surety member not found: " + oldSuretyMemberCode));
        Member newSurety = memberRepository.findByMemberCode(newSuretyMemberCode)
                .orElseThrow(() -> new IllegalArgumentException("New surety member not found: " + newSuretyMemberCode));

        // Validate new surety limit
        if (newSurety.getActiveSuretyCount() >= 3) {
            throw new IllegalStateException("Proposed surety " + newSurety.getFullName() +
                    " already has reached maximum allowed guarantees (3)");
        }

        SuretyReplacement req = new SuretyReplacement();
        req.setLoanAccountNo(loanAccountNo);
        req.setBorrowerMemberCode(loan.getMemberCode());
        req.setBorrowerName(loan.getMemberName());
        req.setOldSuretyMemberCode(oldSuretyMemberCode);
        req.setOldSuretyName(oldSurety.getFullName());
        req.setNewSuretyMemberCode(newSuretyMemberCode);
        req.setNewSuretyName(newSurety.getFullName());
        req.setReasonForReplacement(reason);
        req.setStatus("PENDING");

        return suretyReplacementRepository.save(req);
    }

    @Override
    @Transactional
    public SuretyReplacement approveSuretyReplacement(String replacementId, String approvedBy) {
        SuretyReplacement rep = suretyReplacementRepository.findById(replacementId)
                .orElseThrow(() -> new IllegalArgumentException("Replacement record not found: " + replacementId));

        Loan loan = findByLoanAccountNo(rep.getLoanAccountNo());
        Member oldSurety = memberRepository.findByMemberCode(rep.getOldSuretyMemberCode()).orElse(null);
        Member newSurety = memberRepository.findByMemberCode(rep.getNewSuretyMemberCode()).orElse(null);

        if (oldSurety != null) {
            oldSurety.setActiveSuretyCount(Math.max(0, oldSurety.getActiveSuretyCount() - 1));
            memberRepository.save(oldSurety);
        }
        if (newSurety != null) {
            newSurety.setActiveSuretyCount(newSurety.getActiveSuretyCount() + 1);
            memberRepository.save(newSurety);
        }

        // Update loan sureties list
        if (loan.getSureties() != null) {
            for (Loan.LoanSurety s : loan.getSureties()) {
                if (s.getSuretyMemberCode().equalsIgnoreCase(rep.getOldSuretyMemberCode())) {
                    s.setSuretyMemberCode(rep.getNewSuretyMemberCode());
                    s.setSuretyMemberName(newSurety != null ? newSurety.getFullName() : "");
                    s.setSuretyEmployeeCode(newSurety != null ? newSurety.getEmployeeCode() : "");
                    s.setSuretyBasicPay(newSurety != null ? newSurety.getBasicPay() : BigDecimal.ZERO);
                    s.setStatus("CONFIRMED");
                }
            }
            loanRepository.save(loan);
        }

        rep.setStatus("APPROVED");
        rep.setApprovedBy(approvedBy);
        rep.setApprovalDate(LocalDate.now());

        auditLogRepository.save(new AuditLog(approvedBy, "SURETY_REPLACED", "Loan", loan.getLoanAccountNo(),
                "Approved surety replacement from " + rep.getOldSuretyMemberCode() + " to " + rep.getNewSuretyMemberCode()));

        return suretyReplacementRepository.save(rep);
    }

    @Override
    public Loan findByLoanAccountNo(String loanAccountNo) {
        return loanRepository.findByLoanAccountNo(loanAccountNo)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found: " + loanAccountNo));
    }

    @Override
    public List<Loan> findLoansByMemberCode(String memberCode) {
        return loanRepository.findByMemberCode(memberCode);
    }

    @Override
    public List<Loan> findActiveLoans() {
        return loanRepository.findByStatus("ACTIVE");
    }

    @Override
    public Page<Loan> listLoans(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return loanRepository.findByLoanAccountNoContainingIgnoreCaseOrMemberCodeContainingIgnoreCaseOrMemberNameContainingIgnoreCase(
                    search, search, search, pageable);
        }
        return loanRepository.findAll(pageable);
    }

    @Override
    public List<LoanTransaction> getLoanLedger(String loanAccountNo) {
        return loanTransactionRepository.findByLoanAccountNoOrderByTxnDateDesc(loanAccountNo);
    }
}
