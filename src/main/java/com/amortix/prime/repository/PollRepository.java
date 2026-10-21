package com.amortix.prime.repository;

import com.amortix.prime.model.Poll;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollRepository extends MongoRepository<Poll, String> {
    List<Poll> findByActiveTrueOrderByStartDateDesc();
}
