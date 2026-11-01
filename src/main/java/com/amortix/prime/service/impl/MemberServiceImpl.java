package com.amortix.prime.service.impl;

import com.amortix.prime.model.*;
import com.amortix.prime.repository.*;
import com.amortix.prime.service.AccountingService;
import com.amortix.prime.service.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final DepositAccountRepository depositAccountRepository;
    private final ThriftTransactionRepository thriftTransactionRepository;
    private final AccountingService accountingService;
    private final AuditLogRepository auditLogRepository;

    public MemberServiceImpl(MemberRepository memberRepository,
                             LoanRepository loanRepository,
                             DepositAccountRepository depositAccountRepository,
                             ThriftTransactionRepository thriftTransactionRepository,
                             AccountingService accountingService,
                             AuditLogRepository auditLogRepository) {
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
        this.depositAccountRepository = depositAccountRepository;
        this.thriftTransactionRepository = thriftTransactionRepository;
        this.accountingService = accountingService;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public Member registerMember(Member member) {
        if (member.getMemberCode() == null || member.getMemberCode().isBlank()) {
            long count = memberRepository.count() + 1001;
            member.setMemberCode("MEM-" + count);
        }
        member.setCreatedDate(LocalDateTime.now());
        member.setUpdatedDate(LocalDateTime.now());
        Member saved = memberRepository.save(member);

        auditLogRepository.save(new AuditLog("SYSTEM", "MEMBER_REGISTERED", "Member", saved.getMemberCode(),
                "Registered member: " + saved.getFullName()));
        return saved;
    }

    @Override
    @Transactional
    public Member updateMember(Member member) {
        member.setUpdatedDate(LocalDateTime.now());
        return memberRepository.save(member);
    }

    @Override
    public Member findByMemberCode(String memberCode) {
        return memberRepository.findByMemberCode(memberCode)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with code: " + memberCode));
    }

    @Override
    public Member findById(String id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + id));
    }

    @Override
    public Page<Member> listMembers(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return memberRepository.findByFullNameContainingIgnoreCaseOrMemberCodeContainingIgnoreCaseOrEmployeeCodeContainingIgnoreCase(
                    search, search, search, pageable);
        }
        return memberRepository.findAll(pageable);
    }

    @Override
    public List<Member> listActiveMembers() {
        return memberRepository.findByStatus("ACTIVE");
    }

    @Override
    public boolean existsByMemberCode(String memberCode) {
        return memberRepository.existsByMemberCode(memberCode);
    }

    @Override
    @Transactional
    public void addShareCapital(String memberCode, int shares, BigDecimal shareAmount) {
        Member member = findByMemberCode(memberCode);
        member.setTotalShares(member.getTotalShares() + shares);
        member.setShareCapitalBalance(member.getShareCapitalBalance().add(shareAmount));
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void updateThriftSubscription(String memberCode, BigDecimal newMonthlyAmount) {
        Member member = findByMemberCode(memberCode);
        member.setMonthlyThriftAmount(newMonthlyAmount);
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public MemberSettlementResult calculateAndExecuteSettlement(String memberCode, String remarks, String operatorUser) {
        Member member = findByMemberCode(memberCode);

        // Prevent duplicate settlement (Bug #13 fix)
        if ("SETTLED".equalsIgnoreCase(member.getStatus())) {
            throw new IllegalStateException("Member [" + memberCode + "] is already marked as SETTLED.");
        }

        BigDecimal shareRefund = member.getShareCapitalBalance() != null ? member.getShareCapitalBalance() : BigDecimal.ZERO;
        BigDecimal thriftRefund = member.getThriftBalance() != null ? member.getThriftBalance() : BigDecimal.ZERO;

        // Calculate active deposit payouts
        List<DepositAccount> activeDeposits = depositAccountRepository.findByMemberCodeAndStatus(memberCode, "ACTIVE");
        BigDecimal depositRefund = BigDecimal.ZERO;
        for (DepositAccount dep : activeDeposits) {
            depositRefund = depositRefund.add(dep.getPrincipalAmount()).add(dep.getAccumulatedInterest());
            dep.setStatus("SETTLED_WITH_MEMBER");
            dep.setRemarks("Closed during member full and final settlement");
            depositAccountRepository.save(dep);
        }

        // Calculate outstanding loan deductions
        List<Loan> activeLoans = loanRepository.findByMemberCodeAndStatus(memberCode, "ACTIVE");
        BigDecimal loanPrincipalDed = BigDecimal.ZERO;
        BigDecimal loanInterestDed = BigDecimal.ZERO;

        for (Loan loan : activeLoans) {
            loanPrincipalDed = loanPrincipalDed.add(loan.getOutstandingPrincipal());
            loanInterestDed = loanInterestDed.add(loan.getOutstandingInterest());
            loan.setStatus("SETTLED_WITH_MEMBER");
            loan.setOutstandingPrincipal(BigDecimal.ZERO);
            loan.setOutstandingInterest(BigDecimal.ZERO);
            loan.setRemarks("Liquidated during member settlement");
            loanRepository.save(loan);
        }

        BigDecimal grossRefund = shareRefund.add(thriftRefund).add(depositRefund);
        BigDecimal grossDeductions = loanPrincipalDed.add(loanInterestDed);
        BigDecimal netPayable = grossRefund.subtract(grossDeductions);

        // Prevent Negative Net Payable Overpayment (Bug #13 fix)
        if (netPayable.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Cannot execute settlement payout: Outstanding loan liabilities (₹"
                    + grossDeductions + ") exceed member's total savings and deposits (₹" + grossRefund
                    + "). Net deficit of ₹" + netPayable.abs() + " must be cleared before settlement.");
        }

        // Mark member settled
        member.setStatus("SETTLED");
        member.setShareCapitalBalance(BigDecimal.ZERO);
        member.setThriftBalance(BigDecimal.ZERO);
        memberRepository.save(member);

        // Create settlement payment voucher via AccountingService
        Voucher settlementVoucher = new Voucher();
        settlementVoucher.setVoucherType("PAYMENT");
        settlementVoucher.setPaymentMode("BANK_TRANSFER");
        settlementVoucher.setMemberCode(memberCode);
        settlementVoucher.setMemberName(member.getFullName());
        settlementVoucher.setAmount(netPayable);
        settlementVoucher.setDebitAccountCode("3001"); // Thrift Liability DEBIT
        settlementVoucher.setCreditAccountCode("1002"); // Bank Account CREDIT
        settlementVoucher.setNarration("Full and final cooperative settlement for " + member.getFullName() + ": " + remarks);
        accountingService.createVoucher(settlementVoucher, operatorUser);

        auditLogRepository.save(new AuditLog(operatorUser, "MEMBER_SETTLEMENT", "Member", memberCode,
                "Executed final settlement. Net payout: " + netPayable));

        return new MemberSettlementResult(memberCode, member.getFullName(), shareRefund, thriftRefund,
                depositRefund, loanPrincipalDed, loanInterestDed, netPayable, "SETTLED");
    }
}
