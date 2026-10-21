package com.amortix.prime.repository;

import com.amortix.prime.model.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends MongoRepository<Loan, String> {
    Optional<Loan> findByLoanAccountNo(String loanAccountNo);
    List<Loan> findByMemberCode(String memberCode);
    List<Loan> findByMemberCodeAndStatus(String memberCode, String status);
    List<Loan> findByStatus(String status);
    Page<Loan> findByStatus(String status, Pageable pageable);

    Page<Loan> findByLoanAccountNoContainingIgnoreCaseOrMemberCodeContainingIgnoreCaseOrMemberNameContainingIgnoreCase(
            String loanNo, String memberCode, String memberName, Pageable pageable);

    long countByStatus(String status);
}
