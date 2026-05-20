package com.example.civicpulse.controller;

import com.example.civicpulse.blockchain.VoteBlock;
import com.example.civicpulse.repo.VoteBlockRepository;
import com.example.civicpulse.repo.CandidateRepository;
import com.example.civicpulse.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class AdminController {

    private final AdminService service;
    private final VoteBlockRepository blockRepo;
    private final CandidateRepository candidateRepo;

    public AdminController(AdminService service, VoteBlockRepository blockRepo, CandidateRepository candidateRepo) {
        this.service = service;
        this.blockRepo = blockRepo;
        this.candidateRepo = candidateRepo;
    }

    @DeleteMapping("/reset")
    public String resetSystem() {
        service.resetSystem();
        return "System reset successful";
    }

    @DeleteMapping("/reset-votes")
    public String resetVotes() {
        service.resetVotes();
        return "Votes reset successful";
    }

    @GetMapping("/votes")
    public ResponseEntity<?> getAllVotes() {
        try {
            List<Map<String, Object>> allVotes = new ArrayList<>();
            List<VoteBlock> blocks = blockRepo.findAll();
            int blockNum = 1;
            for (VoteBlock b : blocks) {
                if (b.getVoterHashes() != null) {
                    for (int i = 0; i < b.getVoterHashes().size(); i++) {
                        Map<String, Object> vote = new HashMap<>();
                        vote.put("voterId", b.getVoterHashes().get(i).substring(0, 8) + "...");

                        String candId = b.getCandidateIds().get(i);
                        String candName = candId;
                        if (!"NOTA".equalsIgnoreCase(candId)) {
                            var candOpt = candidateRepo.findByCandidateId(candId);
                            if (candOpt.isPresent()) {
                                candName = candOpt.get().getName();
                            }
                        }
                        vote.put("candidateName", candName);
                        vote.put("timestamp", b.getTimestamp());
                        vote.put("txHash", b.getHash() != null ? b.getHash() : "Pending...");
                        vote.put("block", blockNum);

                        allVotes.add(vote);
                    }
                }
                blockNum++;
            }
            return ResponseEntity.ok(allVotes);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ─────────────────────────────────────────
    // GET BLOCKCHAIN BLOCKS (for block explorer)
    // ─────────────────────────────────────────
    @GetMapping("/blocks")
    public ResponseEntity<?> getBlocks() {
        try {
            List<VoteBlock> blocks = blockRepo.findAll();
            List<Map<String, Object>> result = new ArrayList<>();
            int blockNum = 1;

            for (VoteBlock b : blocks) {
                Map<String, Object> blockData = new HashMap<>();
                blockData.put("blockNumber", blockNum);
                blockData.put("blockId", b.getBlockId());
                blockData.put("hash", b.getHash() != null ? b.getHash() : "Pending...");
                blockData.put("previousHash", b.getPreviousHash());
                blockData.put("timestamp", b.getTimestamp());
                blockData.put("voteCount", b.getVoteCount());
                blockData.put("sealed", b.isSealed());

                // Resolve candidate names
                List<Map<String, String>> votes = new ArrayList<>();
                if (b.getVoterHashes() != null) {
                    for (int i = 0; i < b.getVoterHashes().size(); i++) {
                        Map<String, String> v = new HashMap<>();
                        v.put("voterHash", b.getVoterHashes().get(i).substring(0, 12) + "...");
                        String candId = b.getCandidateIds().get(i);
                        String candName = candId;
                        if (!"NOTA".equalsIgnoreCase(candId)) {
                            var opt = candidateRepo.findByCandidateId(candId);
                            if (opt.isPresent()) candName = opt.get().getName();
                        }
                        v.put("candidateId", candId);
                        v.put("candidateName", candName);
                        votes.add(v);
                    }
                }
                blockData.put("votes", votes);
                result.add(blockData);
                blockNum++;
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}