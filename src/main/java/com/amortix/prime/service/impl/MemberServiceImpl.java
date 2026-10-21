package com.amortix.prime.service.impl;

import com.amortix.prime.model.*;
import com.amortix.prime.repository.*;
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
    private final VoucherRepository voucherRepository;
    private final AuditLogRepository auditLogRepository;

    public MemberServiceImpl(MemberRepository memberRepository,
                             LoanRepository loanRepository,
                             DepositAccountRepository depositAccountRepository,
                             ThriftTransactionRepository thriftTransactionRepository,
                             VoucherRepository voucherRepository,
                             AuditLogRepository auditLogRepository) {
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
        this.depositAccountRepository = depositAccountRepository;
        this.thriftTransactionRepository = thriftTransactionRepository;
        this.voucherRepository = voucherRepository;
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

        BigDecimal shareRefund = member.getShareCapitalBalance();
        BigDecimal thriftRefund = member.getThriftBalance();

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

        // Mark member settled
        member.setStatus("SETTLED");
        member.setShareCapitalBalance(BigDecimal.ZERO);
        member.setThriftBalance(BigDecimal.ZERO);
        memberRepository.save(member);

        // Create settlement payment voucher
        Voucher settlementVoucher = new Voucher();
        settlementVoucher.setVoucherNo("SETTLE-" + memberCode + "-" + System.currentTimeMillis() % 100000);
        settlementVoucher.setVoucherType("PAYMENT");
        settlementVoucher.setMemberCode(memberCode);
        settlementVoucher.setMemberName(member.getFullName());
        settlementVoucher.setAmount(netPayable.max(BigDecimal.ZERO));
        settlementVoucher.setNarration("Full and final cooperative settlement for " + member.getFullName() + ": " + remarks);
        settlementVoucher.setPreparedBy(operatorUser);
        voucherRepository.save(settlementVoucher);

        auditLogRepository.save(new AuditLog(operatorUser, "MEMBER_SETTLEMENT", "Member", memberCode,
                "Executed final settlement. Net payout: " + netPayable));

        return new MemberSettlementResult(memberCode, member.getFullName(), shareRefund, thriftRefund,
                depositRefund, loanPrincipalDed, loanInterestDed, netPayable, "SETTLED");
    }
}
