package com.amortix.prime.repository;

import com.amortix.prime.model.DailyAccountProcessing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyAccountProcessingRepository extends JpaRepository<DailyAccountProcessing, String> {
    Optional<DailyAccountProcessing> findByProcessingDate(LocalDate date);
    Optional<DailyAccountProcessing> findTopByOrderByProcessingDateDesc();
}
