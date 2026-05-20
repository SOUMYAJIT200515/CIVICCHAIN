package com.example.civicpulse.repo;

import com.example.civicpulse.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    // 🔎 search by candidateId (string like CAND001)
    Optional<Candidate> findByCandidateId(String candidateId);

    // 🔎 search by name (useful for UI search)
    List<Candidate> findByNameContainingIgnoreCase(String name);

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE candidate", nativeQuery = true)
    void resetTable();
}