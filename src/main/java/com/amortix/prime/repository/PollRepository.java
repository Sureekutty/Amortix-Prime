package com.amortix.prime.repository;

import com.amortix.prime.model.Poll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollRepository extends JpaRepository<Poll, String> {
    List<Poll> findByActiveTrueOrderByStartDateDesc();
}
