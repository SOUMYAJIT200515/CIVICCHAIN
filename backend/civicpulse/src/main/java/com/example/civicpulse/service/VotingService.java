package com.example.civicpulse.service;

import com.example.civicpulse.model.Candidate;
import com.example.civicpulse.model.Voter;
import com.example.civicpulse.repo.CandidateRepository;
import com.example.civicpulse.repo.VoterRepository;
import com.example.civicpulse.repo.VotingRepository;
import com.example.civicpulse.state.VotingState;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VotingService {

    private final VoterRepository voterRepo;
    private final CandidateRepository candidateRepo;
    private final VotingRepository votingRepo;

    public VotingService(
            VoterRepository voterRepo,
            CandidateRepository candidateRepo,
            VotingRepository votingRepo
    ) {
        this.voterRepo = voterRepo;
        this.candidateRepo = candidateRepo;
        this.votingRepo = votingRepo;
    }

    // ─────────────────────────────────────────
    // RECORD VOTE
    // ─────────────────────────────────────────
    @Transactional
    public void recordVote(String voterId, String candidateId) {

        VotingState state = getStatus();

        if (!"OPEN".equals(state.getStatus())) {
            throw new IllegalStateException("Voting is closed");
        }

        Voter voter = voterRepo.findByVoterId(voterId)
                .orElseThrow(() ->
                        new RuntimeException("Voter not found"));

        if (voter.isHasVoted()) {
            throw new IllegalStateException("Already voted");
        }

        Candidate candidate = candidateRepo.findByCandidateId(candidateId)
                .orElseThrow(() ->
                        new RuntimeException("Candidate not found"));

        candidate.setVoteCount(
                candidate.getVoteCount() + 1
        );

        candidateRepo.save(candidate);

        voter.setHasVoted(true);

        voterRepo.save(voter);
    }

    // ─────────────────────────────────────────
    // OPEN VOTING
    // ─────────────────────────────────────────
    public VotingState openVoting(Integer durationMinutes) {

        VotingState state =
                votingRepo.findById(1L)
                        .orElse(new VotingState());

        state.setId(1L);

        state.setStatus("OPEN");

        state.setDurationMinutes(durationMinutes);

        LocalDateTime endTime =
                LocalDateTime.now()
                        .plusMinutes(durationMinutes);

        state.setEndTime(endTime);

        VotingState saved =
                votingRepo.save(state);

        System.out.println("=================================");
        System.out.println("VOTING OPENED");
        System.out.println("END TIME: " + endTime);
        System.out.println("=================================");

        return saved;
    }

    // ─────────────────────────────────────────
    // LOCK VOTING
    // ─────────────────────────────────────────
    public VotingState lockVoting() {

        VotingState state =
                votingRepo.findById(1L)
                        .orElse(new VotingState());

        state.setId(1L);

        state.setStatus("LOCKED");

        state.setEndTime(LocalDateTime.now());

        VotingState saved =
                votingRepo.save(state);

        System.out.println("=================================");
        System.out.println("VOTING LOCKED");
        System.out.println("=================================");

        return saved;
    }

    // ─────────────────────────────────────────
    // GET STATUS
    // ─────────────────────────────────────────
    public VotingState getStatus() {

        VotingState state =
                votingRepo.findById(1L)
                        .orElse(null);

        // NEVER FOUND
        if (state == null) {

            VotingState defaultState =
                    new VotingState();

            defaultState.setId(1L);

            defaultState.setStatus("LOCKED");

            defaultState.setEndTime(
                    LocalDateTime.now()
            );

            votingRepo.save(defaultState);

            return defaultState;
        }

        // AUTO LOCK EXPIRED
        if (
                "OPEN".equals(state.getStatus())
                        &&
                        state.getEndTime() != null
                        &&
                        LocalDateTime.now()
                                .isAfter(state.getEndTime())
        ) {

            state.setStatus("LOCKED");

            votingRepo.save(state);

            System.out.println("⏰ Voting auto locked");
        }

        return state;
    }

}