package com.amortix.prime.service;

import com.amortix.prime.model.ThriftTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ThriftService {
    ThriftTransaction creditMonthlyThrift(String memberCode, BigDecimal amount, String runCode, String operatorUser);
    ThriftInterestRunResult calculateAndCreditAnnualThriftInterest(double annualInterestRate, String operatorUser);
    List<ThriftTransaction> getMemberThriftLedger(String memberCode);
    Page<ThriftTransaction> getMemberThriftLedgerPaged(String memberCode, Pageable pageable);

    record ThriftInterestRunResult(int totalMembersProcessed, BigDecimal totalInterestDistributed, String runDateStr) {}
}
