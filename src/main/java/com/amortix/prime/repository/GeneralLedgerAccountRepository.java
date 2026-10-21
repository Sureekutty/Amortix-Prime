package com.amortix.prime.repository;

import com.amortix.prime.model.GeneralLedgerAccount;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GeneralLedgerAccountRepository extends MongoRepository<GeneralLedgerAccount, String> {
    Optional<GeneralLedgerAccount> findByAccountCode(String accountCode);
    List<GeneralLedgerAccount> findByAccountType(String accountType);
    List<GeneralLedgerAccount> findByActiveTrueOrderByAccountCodeAsc();
}
