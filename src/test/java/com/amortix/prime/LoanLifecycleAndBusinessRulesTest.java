package com.amortix.prime;

import com.amortix.prime.model.Loan;
import com.amortix.prime.model.Member;
import com.amortix.prime.repository.MemberRepository;
import com.amortix.prime.service.LoanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class LoanLifecycleAndBusinessRulesTest {

    @Autowired
    private LoanService loanService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("Self-Guaranty Violation: Borrower cannot act as their own guarantor")
    void applyForLoan_selfGuarantor_throwsException() {
        Loan loan = new Loan();
        loan.setMemberCode("MEM-1001");
        loan.setLoanType("LTL");
        loan.setSanctionAmount(new BigDecimal("100000.00"));
        loan.setTenureMonths(24);
        loan.getSureties().add(new Loan.LoanSurety("MEM-1001", "Sureendhar J", "EMP-9821", new BigDecimal("65000.00")));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> loanService.applyForLoan(loan));
        assertTrue(ex.getMessage().contains("Self-Guaranty Violation"));
    }

    @Test
    @DisplayName("Duplicate Guarantor Violation: Same surety cannot be listed twice on one loan")
    void applyForLoan_duplicateGuarantor_throwsException() {
        Loan loan = new Loan();
        loan.setMemberCode("MEM-1001");
        loan.setLoanType("LTL");
        loan.setSanctionAmount(new BigDecimal("100000.00"));
        loan.setTenureMonths(24);
        loan.getSureties().add(new Loan.LoanSurety("MEM-1002", "Kavitha Ramesh", "EMP-9822", new BigDecimal("52000.00")));
        loan.getSureties().add(new Loan.LoanSurety("MEM-1002", "Kavitha Ramesh", "EMP-9822", new BigDecimal("52000.00")));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> loanService.applyForLoan(loan));
        assertTrue(ex.getMessage().contains("Duplicate Guarantor Violation"));
    }

    @Test
    @DisplayName("Service Period Rule: Member with less than 6 months service cannot apply for loan")
    void applyForLoan_insufficientService_throwsException() {
        Member newMember = new Member();
        newMember.setMemberCode("MEM-NEW-1");
        newMember.setFullName("Junior Probationer");
        newMember.setJoiningDate(LocalDate.now().minusMonths(2)); // Only 2 months service
        newMember.setBasicPay(new BigDecimal("40000.00"));
        newMember.setStatus("ACTIVE");
        memberRepository.save(newMember);

        Loan loan = new Loan();
        loan.setMemberCode("MEM-NEW-1");
        loan.setLoanType("LTL");
        loan.setSanctionAmount(new BigDecimal("100000.00"));
        loan.setTenureMonths(24);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> loanService.applyForLoan(loan));
        assertTrue(ex.getMessage().contains("Service Rule Violation"));
    }

    @Test
    @DisplayName("Loan Lifecycle: Cannot disburse loan unless it is in APPROVED status")
    void disburseLoan_notApproved_throwsException() {
        // LTL-10001 is already ACTIVE in sample database
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> loanService.sanctionAndDisburseLoan("LTL-10001", "BANK_TRANSFER", "UTR-999", "admin"));
        assertTrue(ex.getMessage().contains("Cannot disburse loan with status [ACTIVE]"));
    }

    @Test
    @DisplayName("Loan Lifecycle: Cannot approve loan that is already ACTIVE")
    void approveLoan_alreadyActive_throwsException() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> loanService.approveLoan("LTL-10001", "admin"));
        assertTrue(ex.getMessage().contains("Only APPLIED loans can be approved"));
    }
}
