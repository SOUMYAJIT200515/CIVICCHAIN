package com.example.civicpulse.service;

import com.example.civicpulse.model.Candidate;
import com.example.civicpulse.model.Voter;
import com.example.civicpulse.state.VotingState;
import com.example.civicpulse.repo.CandidateRepository;
import com.example.civicpulse.repo.VoteBlockRepository;
import com.example.civicpulse.repo.VoterRepository;
import com.example.civicpulse.repo.VotingRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final CandidateRepository candidateRepo;
    private final VoterRepository voterRepo;
    private final VotingRepository votingRepo;
    private final VoteBlockRepository blockRepo;

    public AdminService(
            CandidateRepository candidateRepo,
            VoterRepository voterRepo,
            VotingRepository votingRepo,
            VoteBlockRepository blockRepo
    ) {
        this.candidateRepo = candidateRepo;
        this.voterRepo = voterRepo;
        this.votingRepo = votingRepo;
        this.blockRepo = blockRepo;
    }

    /**
     * Resets only votes: deletes all blockchain blocks,
     * sets candidate vote_count to 0, and sets voter has_voted to false.
     */
    @Transactional
    public void resetVotes() {

        // 1. Delete all blockchain blocks (and their @ElementCollection join tables)
        blockRepo.deleteAll();

        // 2. Reset all candidate vote counts to 0
        List<Candidate> candidates = candidateRepo.findAll();
        for (Candidate c : candidates) {
            c.setVoteCount(0);
            candidateRepo.save(c);
        }

        // 3. Reset all voter hasVoted flags to false
        List<Voter> voters = voterRepo.findAll();
        for (Voter v : voters) {
            v.setHasVoted(false);
            voterRepo.save(v);
        }
    }

    @Transactional
    public void resetSystem() {

        // 1. Reset votes first (blocks, vote counts, hasVoted flags)
        resetVotes();

        // 2. Clear all data
        candidateRepo.deleteAll();
        voterRepo.deleteAll();

        // 3. Reset voting state (single-row table design)
        VotingState state = votingRepo.findById(1L)
                .orElseGet(() -> {
                    VotingState newState = new VotingState();
                    newState.setId(1L);
                    return newState;
                });

        state.setStatus("LOCKED");
        state.setEndTime(null);
        state.setDurationMinutes(0);

        votingRepo.save(state);
    }
}