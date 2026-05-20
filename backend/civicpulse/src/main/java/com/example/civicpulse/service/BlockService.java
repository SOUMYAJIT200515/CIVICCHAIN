package com.example.civicpulse.service;

import com.example.civicpulse.blockchain.HashUtil;
import com.example.civicpulse.blockchain.VoteBlock;
import com.example.civicpulse.repo.VoteBlockRepository;
import com.example.civicpulse.model.Candidate;
import com.example.civicpulse.model.Voter;
import com.example.civicpulse.repo.CandidateRepository;
import com.example.civicpulse.repo.VoterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class BlockService {

    private final VoteBlockRepository blockRepo;
    private final VoterRepository voterRepo;
    private final CandidateRepository candidateRepo;

    public BlockService(
            VoteBlockRepository blockRepo,
            VoterRepository voterRepo,
            CandidateRepository candidateRepo
    ) {
        this.blockRepo = blockRepo;
        this.voterRepo = voterRepo;
        this.candidateRepo = candidateRepo;
    }

    // ─────────────────────────────────────────────
    // CORE VOTING LOGIC
    // ─────────────────────────────────────────────
    @Transactional
    public boolean castVote(String voterId, String candidateId) {

        // 1. validate voter (FIXED)
        Voter voter = voterRepo.findByVoterId(voterId)
                .orElseThrow(() -> new RuntimeException("Voter not found"));

        if (voter.isHasVoted()) {
            return false;
        }

        // 2. anonymous hash
        String voterHash = HashUtil.sha256(voterId);

        // 3. candidate fetch
        Candidate candidate = null;

        if (!"NOTA".equalsIgnoreCase(candidateId)) {
            candidate = candidateRepo.findByCandidateId(candidateId)
                    .orElseThrow(() -> new RuntimeException("Candidate not found"));
        }

        // 4. latest block
        VoteBlock block = blockRepo.findTopByOrderByTimestampDesc();

        if (block == null || block.isSealed() || block.getVoteCount() >= 10) {
            block = createNewBlock(block);
        }

        // 5. safety init
        if (block.getVoterHashes() == null) {
            block.setVoterHashes(new ArrayList<>());
        }

        if (block.getCandidateIds() == null) {
            block.setCandidateIds(new ArrayList<>());
        }

        // 6. add vote
        block.getVoterHashes().add(voterHash);
        block.getCandidateIds().add(candidateId);
        block.setVoteCount(block.getVoteCount() + 1);

        // 7. mark voter as voted (applies to ALL votes including NOTA)
        voter.setHasVoted(true);
        voterRepo.save(voter);

        // 8. update candidate vote count (skip for NOTA)
        if (candidate != null) {
            candidate.setVoteCount(candidate.getVoteCount() + 1);
            candidateRepo.save(candidate);
        }

        // 9. seal block if full
        if (block.getVoteCount() >= 10) {
            sealBlock(block);
        }

        // 10. save block
        blockRepo.save(block);

        return true;
    }

    // ─────────────────────────────────────────────
    // CREATE BLOCK
    // ─────────────────────────────────────────────
    private VoteBlock createNewBlock(VoteBlock previous) {

        VoteBlock block = new VoteBlock();

        String prevHash = (previous == null)
                ? "GENESIS"
                : previous.getHash();

        block.setPreviousHash(prevHash);
        block.setTimestamp(System.currentTimeMillis());
        block.setVoteCount(0);
        block.setSealed(false);

        block.setVoterHashes(new ArrayList<>());
        block.setCandidateIds(new ArrayList<>());

        return block;
    }

    // ─────────────────────────────────────────────
    // SEAL BLOCK
    // ─────────────────────────────────────────────
    private void sealBlock(VoteBlock block) {

        String rawData =
                block.getPreviousHash()
                        + block.getTimestamp()
                        + block.getVoterHashes()
                        + block.getCandidateIds()
                        + block.getVoteCount();

        String hash = HashUtil.sha256(rawData);

        block.setHash(hash);
        block.setSealed(true);
    }

    // ─────────────────────────────────────────────
    // REMOVE VOTER'S VOTES (when voter is deleted)
    // ─────────────────────────────────────────────
    @Transactional
    public void removeVoterVotes(String voterId) {

        String voterHash = HashUtil.sha256(voterId);

        List<VoteBlock> blocks = blockRepo.findAll();

        for (VoteBlock block : blocks) {
            if (block.getVoterHashes() == null) continue;

            // Find all indices matching this voter's hash
            for (int i = block.getVoterHashes().size() - 1; i >= 0; i--) {
                if (voterHash.equals(block.getVoterHashes().get(i))) {

                    // Get candidate and decrement their vote count
                    String candId = block.getCandidateIds().get(i);
                    if (!"NOTA".equalsIgnoreCase(candId)) {
                        candidateRepo.findByCandidateId(candId).ifPresent(c -> {
                            c.setVoteCount(Math.max(0, c.getVoteCount() - 1));
                            candidateRepo.save(c);
                        });
                    }

                    // Remove from block
                    block.getVoterHashes().remove(i);
                    block.getCandidateIds().remove(i);
                    block.setVoteCount(Math.max(0, block.getVoteCount() - 1));
                }
            }

            blockRepo.save(block);
        }
    }
}