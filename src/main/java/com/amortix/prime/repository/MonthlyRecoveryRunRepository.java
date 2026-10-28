package com.amortix.prime.repository;

import com.amortix.prime.model.MonthlyRecoveryRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MonthlyRecoveryRunRepository extends JpaRepository<MonthlyRecoveryRun, String> {
    Optional<MonthlyRecoveryRun> findByRunCode(String runCode);
    Optional<MonthlyRecoveryRun> findByMonthAndYear(int month, int year);
    boolean existsByMonthAndYear(int month, int year);
}
