package com.amortix.prime.service;

import com.amortix.prime.model.DepositAccount;
import com.amortix.prime.model.DepositTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface DepositService {
    DepositAccount openDeposit(DepositAccount deposit, String operatorUser);
    DepositAccount prematureCloseDeposit(String depositAccountNo, String remarks, String operatorUser);
    DepositTransaction recordRcdInstallment(String depositAccountNo, BigDecimal installmentAmount, String receiptNo, String operatorUser);

    DepositAccount findByDepositAccountNo(String depositAccountNo);
    List<DepositAccount> findByMemberCode(String memberCode);
    Page<DepositAccount> listDeposits(String search, Pageable pageable);
    List<DepositTransaction> getDepositTransactions(String depositAccountNo);

    // Calculates preview maturity for frontend preview before submit
    MaturityPreview previewMaturity(String depositType, BigDecimal amount, int tenureMonths, Double customRate);

    record MaturityPreview(BigDecimal principal, Double rate, int tenureMonths, BigDecimal maturityAmount, String maturityDateStr) {}
}
