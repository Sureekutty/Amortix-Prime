package com.amortix.prime.service;

import com.amortix.prime.model.Loan;
import com.amortix.prime.model.LoanTransaction;
import com.amortix.prime.model.SuretyReplacement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface LoanService {
    Loan applyForLoan(Loan loan);
    Loan approveLoan(String loanAccountNo, String approvedBy);
    Loan sanctionAndDisburseLoan(String loanAccountNo, String paymentMode, String chequeOrRefNo, String officerUser);
    Loan rejectLoan(String loanAccountNo, String reason, String officerUser);

    LoanTransaction recordLoanRepayment(String loanAccountNo, BigDecimal principalAmount, BigDecimal interestAmount,
                                       String paymentMode, String receiptNo, String remarks, String operatorUser);

    SuretyReplacement requestSuretyReplacement(String loanAccountNo, String oldSuretyMemberCode,
                                               String newSuretyMemberCode, String reason);
    SuretyReplacement approveSuretyReplacement(String replacementId, String approvedBy);

    Loan findByLoanAccountNo(String loanAccountNo);
    List<Loan> findLoansByMemberCode(String memberCode);
    List<Loan> findActiveLoans();
    Page<Loan> listLoans(String search, Pageable pageable);
    List<LoanTransaction> getLoanLedger(String loanAccountNo);
}
