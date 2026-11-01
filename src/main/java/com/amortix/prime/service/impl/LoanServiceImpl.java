package com.amortix.prime.service.impl;

import com.amortix.prime.model.*;
import com.amortix.prime.repository.*;
import com.amortix.prime.service.AccountingService;
import com.amortix.prime.service.LoanService;
import com.amortix.prime.util.FinancialMath;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final MemberRepository memberRepository;
    private final SuretyReplacementRepository suretyReplacementRepository;
    private final AccountingService accountingService;
    private final SocietyRuleRepository societyRuleRepository;
    private final InterestRateRuleRepository interestRateRuleRepository;
    private final AuditLogRepository auditLogRepository;

    public LoanServiceImpl(LoanRepository loanRepository,
                           LoanTransactionRepository loanTransactionRepository,
                           MemberRepository memberRepository,
                           SuretyReplacementRepository suretyReplacementRepository,
                           AccountingService accountingService,
                           SocietyRuleRepository societyRuleRepository,
                           InterestRateRuleRepository interestRateRuleRepository,
                           AuditLogRepository auditLogRepository) {
        this.loanRepository = loanRepository;
        this.loanTransactionRepository = loanTransactionRepository;
        this.memberRepository = memberRepository;
        this.suretyReplacementRepository = suretyReplacementRepository;
        this.accountingService = accountingService;
        this.societyRuleRepository = societyRuleRepository;
        this.interestRateRuleRepository = interestRateRuleRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public Loan applyForLoan(Loan loan) {
        Member member = memberRepository.findByMemberCode(loan.getMemberCode())
                .orElseThrow(() -> new IllegalArgumentException("Borrower member not found: " + loan.getMemberCode()));

        // 1. Minimum Service Period Rule Validation (Bug #12 fix)
        int minServiceMonths = 6;
        SocietyRule serviceRule = societyRuleRepository.findByRuleCode("MIN_SERVICE_MONTHS").orElse(null);
        if (serviceRule != null) {
            try { minServiceMonths = Integer.parseInt(serviceRule.getRuleValue()); } catch (Exception ignored) {}
        }
        if (member.getJoiningDate() != null) {
            long serviceMonths = ChronoUnit.MONTHS.between(member.getJoiningDate(), LocalDate.now());
            if (serviceMonths < minServiceMonths) {
                throw new IllegalStateException("Service Rule Violation: Member has " + serviceMonths +
                        " months of service. Minimum required service period is " + minServiceMonths + " months.");
            }
        }

        // 2. Verification of Basic Pay Multiplier Rule (MAX_LOAN_MULTIPLIER)
        int multiplier = 10;
        SocietyRule multiplierRule = societyRuleRepository.findByRuleCode("MAX_LOAN_MULTIPLIER").orElse(null);
        if (multiplierRule != null) {
            try { multiplier = Integer.parseInt(multiplierRule.getRuleValue()); } catch (Exception ignored) {}
        }
        BigDecimal maxAllowedAmount = member.getBasicPay() != null
                ? member.getBasicPay().multiply(BigDecimal.valueOf(multiplier))
                : BigDecimal.ZERO;

        if (maxAllowedAmount.compareTo(BigDecimal.ZERO) > 0 && loan.getSanctionAmount().compareTo(maxAllowedAmount) > 0) {
            throw new IllegalStateException("Requested loan amount ₹" + loan.getSanctionAmount() +
                    " exceeds maximum allowed limit (10x Basic Pay: ₹" + maxAllowedAmount + ")");
        }

        // 3. Surety Validation (Bug #10 & #11 fix)
        int maxSuretiesPerMember = 3;
        SocietyRule maxSuretyRule = societyRuleRepository.findByRuleCode("MAX_SURETIES_PER_MEMBER").orElse(null);
        if (maxSuretyRule != null) {
            try { maxSuretiesPerMember = Integer.parseInt(maxSuretyRule.getRuleValue()); } catch (Exception ignored) {}
        }

        if (loan.getSureties() != null) {
            Set<String> seenSureties = new HashSet<>();
            for (Loan.LoanSurety s : loan.getSureties()) {
                if (s.getSuretyMemberCode() != null && !s.getSuretyMemberCode().isBlank()) {
                    if (s.getSuretyMemberCode().equalsIgnoreCase(loan.getMemberCode())) {
                        throw new IllegalStateException("Self-Guaranty Violation: Borrower " + loan.getMemberCode() + " cannot act as their own loan surety.");
                    }
                    if (!seenSureties.add(s.getSuretyMemberCode())) {
                        throw new IllegalStateException("Duplicate Guarantor Violation: Surety " + s.getSuretyMemberCode() + " cannot be added multiple times.");
                    }
                    Member suretyMember = memberRepository.findByMemberCode(s.getSuretyMemberCode())
                            .orElseThrow(() -> new IllegalArgumentException("Surety member not found: " + s.getSuretyMemberCode()));
                    if (suretyMember.getActiveSuretyCount() >= maxSuretiesPerMember) {
                        throw new IllegalStateException("Surety " + suretyMember.getFullName() +
                                " has already reached the maximum allowed active guarantees (" + maxSuretiesPerMember + ").");
                    }
                    s.setSuretyMemberName(suretyMember.getFullName());
                    s.setSuretyEmployeeCode(suretyMember.getEmployeeCode());
                    s.setSuretyBasicPay(suretyMember.getBasicPay());
                    s.setStatus("PENDING_SANCTION");
                }
            }
        }

        // 4. Dynamic interest rate lookup
        if (loan.getInterestRate() == null || loan.getInterestRate() <= 0.0) {
            InterestRateRule rateRule = interestRateRuleRepository
                    .findFirstByProductCodeAndActiveTrueOrderByEffectiveFromDateDesc(loan.getLoanType())
                    .orElse(null);
            loan.setInterestRate(rateRule != null ? rateRule.getInterestRate() : 9.5);
        }

        // 5. Monthly EMI calculation
        BigDecimal emi = FinancialMath.calculateReducingBalanceEmi(
                loan.getSanctionAmount(), loan.getInterestRate(), loan.getTenureMonths());
        loan.setMonthlyInstallment(emi);

        // 6. Generate Account No
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

        // Loan Lifecycle Validation (Bug #8 fix)
        if (!"APPLIED".equalsIgnoreCase(loan.getStatus())) {
            throw new IllegalStateException("Cannot approve loan with status [" + loan.getStatus() + "]. Only APPLIED loans can be approved.");
        }

        loan.setStatus("APPROVED");
        loan.setSanctionDate(LocalDate.now());
        loan.setSanctionedBy(approvedBy);
        return loanRepository.save(loan);
    }

    @Override
    @Transactional
    public Loan sanctionAndDisburseLoan(String loanAccountNo, String paymentMode, String chequeOrRefNo, String officerUser) {
        Loan loan = findByLoanAccountNo(loanAccountNo);

        // Loan Lifecycle Validation (Bug #8 fix - prevent duplicate disbursement)
        if (!"APPROVED".equalsIgnoreCase(loan.getStatus())) {
            throw new IllegalStateException("Cannot disburse loan with status [" + loan.getStatus() + "]. Loan must be APPROVED first.");
        }

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
                    s.setStatus("CONFIRMED");
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

        // Create Payment Voucher and update ledger (2001 Member Loans Asset DEBIT, 1001 Cash / 1002 Bank CREDIT)
        Voucher v = new Voucher();
        v.setVoucherType("PAYMENT");
        v.setMemberCode(loan.getMemberCode());
        v.setMemberName(loan.getMemberName());
        v.setAmount(loan.getSanctionAmount());
        v.setPaymentMode(paymentMode);
        v.setInstrumentNo(chequeOrRefNo);
        v.setDebitAccountCode("2001"); // Member Loans Asset (DEBIT increases asset)
        v.setCreditAccountCode("CASH".equalsIgnoreCase(paymentMode) ? "1001" : "1002"); // Cash or Bank CREDIT
        v.setCategory("LOAN_DISBURSEMENT");
        v.setNarration("Disbursement of " + loan.getLoanType() + " account " + loan.getLoanAccountNo());
        accountingService.createVoucher(v, officerUser);

        Loan updated = loanRepository.save(loan);
        auditLogRepository.save(new AuditLog(officerUser, "LOAN_DISBURSED", "Loan", loan.getLoanAccountNo(),
                "Disbursed loan amount: " + loan.getSanctionAmount()));
        return updated;
    }

    @Override
    @Transactional
    public Loan rejectLoan(String loanAccountNo, String reason, String officerUser) {
        Loan loan = findByLoanAccountNo(loanAccountNo);
        if (!"APPLIED".equalsIgnoreCase(loan.getStatus())) {
            throw new IllegalStateException("Cannot reject loan with status [" + loan.getStatus() + "]. Only APPLIED loans can be rejected.");
        }
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

        // Record Receipt Voucher via AccountingService
        Voucher rcp = new Voucher();
        rcp.setVoucherType("RECEIPT");
        rcp.setMemberCode(loan.getMemberCode());
        rcp.setMemberName(loan.getMemberName());
        rcp.setAmount(total);
        rcp.setPaymentMode(paymentMode);
        rcp.setDebitAccountCode("CASH".equalsIgnoreCase(paymentMode) ? "1001" : "1002"); // Cash or Bank DEBIT
        rcp.setCreditAccountCode("2001"); // Member Loans Asset CREDIT (reduces asset)
        rcp.setCategory("LOAN_REPAYMENT");
        rcp.setNarration("Repayment for " + loan.getLoanAccountNo() + " (Principal: " + pri + ", Interest: " + intr + ")");
        accountingService.createVoucher(rcp, operatorUser);

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

        // Surety ownership validation (Bug #10 fix)
        boolean isExistingGuarantor = loan.getSureties() != null && loan.getSureties().stream()
                .anyMatch(s -> s.getSuretyMemberCode().equalsIgnoreCase(oldSuretyMemberCode));
        if (!isExistingGuarantor) {
            throw new IllegalStateException("Surety " + oldSuretyMemberCode + " is not an active guarantor on loan " + loanAccountNo);
        }
        if (newSuretyMemberCode.equalsIgnoreCase(loan.getMemberCode())) {
            throw new IllegalStateException("Borrower " + loan.getMemberCode() + " cannot be proposed as their own surety.");
        }

        // Dynamic rule validation for max sureties (Bug #11 fix)
        int maxSuretiesPerMember = 3;
        SocietyRule maxSuretyRule = societyRuleRepository.findByRuleCode("MAX_SURETIES_PER_MEMBER").orElse(null);
        if (maxSuretyRule != null) {
            try { maxSuretiesPerMember = Integer.parseInt(maxSuretyRule.getRuleValue()); } catch (Exception ignored) {}
        }
        if (newSurety.getActiveSuretyCount() >= maxSuretiesPerMember) {
            throw new IllegalStateException("Proposed surety " + newSurety.getFullName() +
                    " already has reached maximum allowed guarantees (" + maxSuretiesPerMember + ").");
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

        // Duplicate approval prevention (Bug #9 fix)
        if (!"PENDING".equalsIgnoreCase(rep.getStatus())) {
            throw new IllegalStateException("Surety replacement request " + replacementId + " is already " + rep.getStatus());
        }

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
