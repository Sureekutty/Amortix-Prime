package com.amortix.prime.repository;

import com.amortix.prime.model.LoanTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanTransactionRepository extends MongoRepository<LoanTransaction, String> {
    List<LoanTransaction> findByLoanAccountNoOrderByTxnDateDesc(String loanAccountNo);
    List<LoanTransaction> findByMemberCodeOrderByTxnDateDesc(String memberCode);
    List<LoanTransaction> findByTxnDateBetween(LocalDate from, LocalDate to);
    Page<LoanTransaction> findByLoanAccountNoOrderByTxnDateDesc(String loanAccountNo, Pageable pageable);
}
