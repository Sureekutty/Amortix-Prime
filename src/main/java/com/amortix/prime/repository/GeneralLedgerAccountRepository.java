package com.amortix.prime.repository;

import com.amortix.prime.model.GeneralLedgerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GeneralLedgerAccountRepository extends JpaRepository<GeneralLedgerAccount, String> {
    Optional<GeneralLedgerAccount> findByAccountCode(String accountCode);
    List<GeneralLedgerAccount> findByAccountType(String accountType);
    List<GeneralLedgerAccount> findByActiveTrueOrderByAccountCodeAsc();
}
