package com.amortix.prime.repository;

import com.amortix.prime.model.MonthlyRecoveryRun;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MonthlyRecoveryRunRepository extends MongoRepository<MonthlyRecoveryRun, String> {
    Optional<MonthlyRecoveryRun> findByRunCode(String runCode);
    Optional<MonthlyRecoveryRun> findByMonthAndYear(int month, int year);
    boolean existsByMonthAndYear(int month, int year);
}
