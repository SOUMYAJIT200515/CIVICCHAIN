package com.example.civicpulse.controller;

import com.example.civicpulse.service.BlockService;
import com.example.civicpulse.service.VotingService;
import com.example.civicpulse.state.VotingState;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/voting")
@CrossOrigin(origins = "*")
public class VotingController {

    private final VotingService votingService;
    private final BlockService blockService;

    public VotingController(VotingService votingService, BlockService blockService) {
        this.votingService = votingService;
        this.blockService = blockService;
    }

    // ─────────────────────────────────────────
    // CAST VOTE (blockchain path — 10 votes = 1 block)
    // ─────────────────────────────────────────
    @PostMapping("/vote")
    public ResponseEntity<?> castVote(
            @RequestBody Map<String, String> body
    ) {

        try {

            String voterId = body.get("voterId");
            String candidateId = body.get("candidateId");

            if (voterId == null || candidateId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "voterId and candidateId are required");
                return ResponseEntity.badRequest().body(error);
            }

            // Check voting status first
            VotingState state = votingService.getStatus();
            if (!"OPEN".equals(state.getStatus())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Voting is closed");
                return ResponseEntity.badRequest().body(error);
            }

            // Cast vote via BlockService (handles blockchain 10-vote blocks)
            boolean success = blockService.castVote(voterId, candidateId);

            if (!success) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Vote already cast or voter not found");
                return ResponseEntity.badRequest().body(error);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vote recorded on blockchain");

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(error);
        }
    }

    // ─────────────────────────────────────────
    // OPEN VOTING
    // ─────────────────────────────────────────
    @PostMapping("/open")
    public ResponseEntity<?> openVoting(
            @RequestBody Map<String, Integer> body
    ) {

        try {

            Integer durationMinutes =
                    body.get("durationMinutes");

            if (
                    durationMinutes == null ||
                            durationMinutes <= 0
            ) {

                Map<String, Object> error =
                        new HashMap<>();

                error.put("success", false);
                error.put("message", "Invalid duration");

                return ResponseEntity
                        .badRequest()
                        .body(error);
            }

            VotingState state =
                    votingService.openVoting(
                            durationMinutes
                    );

            long expiryMillis =
                    state.getEndTime()
                            .atZone(
                                    ZoneId.systemDefault()
                            )
                            .toInstant()
                            .toEpochMilli();

            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", true);
            response.put("message", "Voting opened");
            response.put("status", state.getStatus());
            response.put("isOpen", true);
            response.put("expiryMillis", expiryMillis);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();

            Map<String, Object> error =
                    new HashMap<>();

            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity
                    .badRequest()
                    .body(error);
        }
    }

    // ─────────────────────────────────────────
    // LOCK VOTING
    // ─────────────────────────────────────────
    @PostMapping("/lock")
    public ResponseEntity<?> lockVoting() {

        try {

            VotingState state =
                    votingService.lockVoting();

            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", true);
            response.put("message", "Voting locked");
            response.put("status", state.getStatus());
            response.put("isOpen", false);
            response.put("expiryMillis", 0);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();

            Map<String, Object> error =
                    new HashMap<>();

            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity
                    .badRequest()
                    .body(error);
        }
    }

    // ─────────────────────────────────────────
    // GET STATUS
    // ─────────────────────────────────────────
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {

        try {

            VotingState state =
                    votingService.getStatus();

            boolean isOpen =
                    "OPEN".equals(
                            state.getStatus()
                    );

            long expiryMillis = 0;

            if (
                    state.getEndTime() != null
            ) {

                expiryMillis =
                        state.getEndTime()
                                .atZone(
                                        ZoneId.systemDefault()
                                )
                                .toInstant()
                                .toEpochMilli();
            }

            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", true);
            response.put("status", state.getStatus());
            response.put("isOpen", isOpen);
            response.put("expiryMillis", expiryMillis);
            response.put(
                    "serverTime",
                    System.currentTimeMillis()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            e.printStackTrace();

            Map<String, Object> error =
                    new HashMap<>();

            error.put("success", false);
            error.put("message", e.getMessage());
            error.put("status", "LOCKED");
            error.put("isOpen", false);
            error.put("expiryMillis", 0);

            return ResponseEntity
                    .badRequest()
                    .body(error);
        }
    }
}