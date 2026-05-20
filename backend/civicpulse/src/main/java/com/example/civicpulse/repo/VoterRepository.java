package com.example.civicpulse.repo;

import com.example.civicpulse.model.Voter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface VoterRepository extends JpaRepository<Voter, Long> {

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE voter", nativeQuery = true)
    void resetTable();

    @Modifying
    @Transactional
    @Query(value = "ALTER TABLE voter AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();

    // Finds the voter using the ID stored in your session
    Optional<Voter> findByVoterId(String voterId);

    boolean existsByVoterId(String voterId);

    Optional<Voter> findByVoterIdAndDob(String voterId, java.time.LocalDate dob);
}