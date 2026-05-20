package com.example.civicpulse.repo;

import com.example.civicpulse.state.VotingState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VotingRepository extends JpaRepository<VotingState, Long> {
}