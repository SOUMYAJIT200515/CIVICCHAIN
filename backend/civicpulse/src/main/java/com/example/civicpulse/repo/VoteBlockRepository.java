package com.example.civicpulse.repo;

import com.example.civicpulse.blockchain.VoteBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteBlockRepository extends JpaRepository<VoteBlock, String> {

    // gets latest block (chain tip)
    VoteBlock findTopByOrderByTimestampDesc();
}