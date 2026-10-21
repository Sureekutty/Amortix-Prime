package com.amortix.prime.repository;

import com.amortix.prime.model.DepositTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepositTransactionRepository extends MongoRepository<DepositTransaction, String> {
    List<DepositTransaction> findByDepositAccountNoOrderByTxnDateDesc(String depositAccountNo);
    List<DepositTransaction> findByMemberCodeOrderByTxnDateDesc(String memberCode);
    Page<DepositTransaction> findByDepositAccountNoOrderByTxnDateDesc(String depositAccountNo, Pageable pageable);
}
