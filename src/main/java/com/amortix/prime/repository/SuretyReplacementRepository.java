package com.amortix.prime.repository;

import com.amortix.prime.model.SuretyReplacement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuretyReplacementRepository extends JpaRepository<SuretyReplacement, String> {
    List<SuretyReplacement> findByStatus(String status);
    List<SuretyReplacement> findByLoanAccountNo(String loanAccountNo);
    List<SuretyReplacement> findByBorrowerMemberCode(String memberCode);
}
