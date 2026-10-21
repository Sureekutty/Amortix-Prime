package com.amortix.prime.service.impl;

import com.amortix.prime.model.AuditLog;
import com.amortix.prime.model.Member;
import com.amortix.prime.model.ThriftTransaction;
import com.amortix.prime.model.Voucher;
import com.amortix.prime.repository.AuditLogRepository;
import com.amortix.prime.repository.MemberRepository;
import com.amortix.prime.repository.ThriftTransactionRepository;
import com.amortix.prime.repository.VoucherRepository;
import com.amortix.prime.service.ThriftService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class ThriftServiceImpl implements ThriftService {

    private final MemberRepository memberRepository;
    private final ThriftTransactionRepository thriftTransactionRepository;
    private final VoucherRepository voucherRepository;
    private final AuditLogRepository auditLogRepository;

    public ThriftServiceImpl(MemberRepository memberRepository,
                             ThriftTransactionRepository thriftTransactionRepository,
                             VoucherRepository voucherRepository,
                             AuditLogRepository auditLogRepository) {
        this.memberRepository = memberRepository;
        this.thriftTransactionRepository = thriftTransactionRepository;
        this.voucherRepository = voucherRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public ThriftTransaction creditMonthlyThrift(String memberCode, BigDecimal amount, String runCode, String operatorUser) {
        Member member = memberRepository.findByMemberCode(memberCode)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberCode));

        BigDecimal newBalance = member.getThriftBalance().add(amount);
        member.setThriftBalance(newBalance);
        memberRepository.save(member);

        ThriftTransaction txn = new ThriftTransaction();
        txn.setMemberCode(memberCode);
        txn.setMemberName(member.getFullName());
        txn.setTxnDate(LocalDate.now());
        txn.setTxnType("MONTHLY_SUBSCRIPTION");
        txn.setAmount(amount);
        txn.setBalanceAfterTxn(newBalance);
        txn.setRecoveryRunId(runCode);
        txn.setRemarks("Monthly Compulsory Thrift Deposit collection");
        txn.setCreatedBy(operatorUser);

        return thriftTransactionRepository.save(txn);
    }

    @Override
    @Transactional
    public ThriftInterestRunResult calculateAndCreditAnnualThriftInterest(double annualInterestRate, String operatorUser) {
        List<Member> activeMembers = memberRepository.findByStatus("ACTIVE");
        int count = 0;
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (Member m : activeMembers) {
            BigDecimal bal = m.getThriftBalance();
            if (bal != null && bal.compareTo(BigDecimal.ZERO) > 0) {
                // Annual interest on cumulative balance
                BigDecimal interestEarned = bal.multiply(BigDecimal.valueOf(annualInterestRate))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                if (interestEarned.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal newBal = bal.add(interestEarned);
                    m.setThriftBalance(newBal);
                    memberRepository.save(m);

                    ThriftTransaction txn = new ThriftTransaction();
                    txn.setMemberCode(m.getMemberCode());
                    txn.setMemberName(m.getFullName());
                    txn.setTxnDate(LocalDate.now());
                    txn.setTxnType("INTEREST_CREDIT");
                    txn.setAmount(interestEarned);
                    txn.setBalanceAfterTxn(newBal);
                    txn.setRemarks("Annual Thrift Interest Credit at " + annualInterestRate + "% p.a.");
                    txn.setCreatedBy(operatorUser);
                    thriftTransactionRepository.save(txn);

                    totalInterest = totalInterest.add(interestEarned);
                    count++;
                }
            }
        }

        // Create Journal Voucher for Society Expense -> Thrift Liability
        Voucher jv = new Voucher();
        jv.setVoucherNo("THRIFT-INT-" + System.currentTimeMillis() % 100000);
        jv.setVoucherType("JOURNAL_VOUCHER");
        jv.setDebitAccountCode("5001-THRIFT_INTEREST_EXPENSE");
        jv.setCreditAccountCode("3001-THRIFT_DEPOSIT_LIABILITY");
        jv.setAmount(totalInterest);
        jv.setNarration("Annual Thrift Interest Accrual for " + count + " active members at " + annualInterestRate + "%");
        jv.setPreparedBy(operatorUser);
        voucherRepository.save(jv);

        auditLogRepository.save(new AuditLog(operatorUser, "THRIFT_INTEREST_RUN", "Thrift", "ALL",
                "Distributed ₹" + totalInterest + " interest to " + count + " members"));

        return new ThriftInterestRunResult(count, totalInterest, LocalDate.now().toString());
    }

    @Override
    public List<ThriftTransaction> getMemberThriftLedger(String memberCode) {
        return thriftTransactionRepository.findByMemberCodeOrderByTxnDateDesc(memberCode);
    }

    @Override
    public Page<ThriftTransaction> getMemberThriftLedgerPaged(String memberCode, Pageable pageable) {
        return thriftTransactionRepository.findByMemberCodeOrderByTxnDateDesc(memberCode, pageable);
    }
}
