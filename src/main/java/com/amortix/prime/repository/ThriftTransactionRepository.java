package com.amortix.prime.repository;

import com.amortix.prime.model.ThriftTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThriftTransactionRepository extends JpaRepository<ThriftTransaction, String> {
    List<ThriftTransaction> findByMemberCodeOrderByTxnDateDesc(String memberCode);
    List<ThriftTransaction> findByRecoveryRunId(String recoveryRunId);
    Page<ThriftTransaction> findByMemberCodeOrderByTxnDateDesc(String memberCode, Pageable pageable);
}
