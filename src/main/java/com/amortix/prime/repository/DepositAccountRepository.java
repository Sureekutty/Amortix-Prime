package com.amortix.prime.repository;

import com.amortix.prime.model.DepositAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepositAccountRepository extends MongoRepository<DepositAccount, String> {
    Optional<DepositAccount> findByDepositAccountNo(String depositAccountNo);
    List<DepositAccount> findByMemberCode(String memberCode);
    List<DepositAccount> findByMemberCodeAndStatus(String memberCode, String status);
    List<DepositAccount> findByDepositTypeAndStatus(String depositType, String status);
    Page<DepositAccount> findByDepositAccountNoContainingIgnoreCaseOrMemberCodeContainingIgnoreCaseOrMemberNameContainingIgnoreCase(
            String depNo, String memberCode, String name, Pageable pageable);
    long countByStatus(String status);
}
