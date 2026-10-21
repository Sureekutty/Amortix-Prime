package com.amortix.prime.repository;

import com.amortix.prime.model.DailyAccountProcessing;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyAccountProcessingRepository extends MongoRepository<DailyAccountProcessing, String> {
    Optional<DailyAccountProcessing> findByProcessingDate(LocalDate date);
    Optional<DailyAccountProcessing> findTopByOrderByProcessingDateDesc();
}
