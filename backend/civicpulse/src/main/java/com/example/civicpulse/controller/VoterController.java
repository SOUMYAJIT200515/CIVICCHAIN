package com.example.civicpulse.controller;

import com.example.civicpulse.model.Voter;
import com.example.civicpulse.repo.VoterRepository;
import com.example.civicpulse.service.BlockService;
import com.example.civicpulse.service.OtpService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/voters")
@CrossOrigin(origins = "*")
public class VoterController {

    private final VoterRepository repo;
    private final OtpService otpService;
    private final BlockService blockService;

    public VoterController(VoterRepository repo, OtpService otpService, BlockService blockService) {
        this.repo = repo;
        this.otpService = otpService;
        this.blockService = blockService;
    }

    // ─────────────────────────────────────────
    // GET ALL VOTERS
    // ─────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> getAllVoters() {

        try {

            List<Voter> voters = repo.findAll();

            return ResponseEntity.ok(voters);

        } catch (Exception e) {

            e.printStackTrace();

            Map<String, Object> error =
                    new HashMap<>();

            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.badRequest()
                    .body(error);
        }
    }

    // ─────────────────────────────────────────
    // GET VOTER — handles both numeric ID and voterId string
    // ─────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getVoter(
            @PathVariable String id
    ) {

        try {

            // Try numeric ID first (admin panel uses v.id)
            try {
                Long numericId = Long.parseLong(id);
                Optional<Voter> voter = repo.findById(numericId);

                if (voter.isEmpty()) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Voter not found");
                    return ResponseEntity.badRequest().body(error);
                }

                return ResponseEntity.ok(voter.get());

            } catch (NumberFormatException e) {
                // Fall back to voterId string (e.g. "VOT1001")
                Optional<Voter> voter = repo.findByVoterId(id);

                if (voter.isEmpty()) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("message", "Voter not found");
                    return ResponseEntity.badRequest().body(error);
                }

                return ResponseEntity.ok(voter.get());
            }

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
    // CREATE VOTER
    // ─────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> createVoter(
            @RequestBody Voter voter
    ) {

        try {

            // prevent duplicate voterId
            if (
                    repo.existsByVoterId(
                            voter.getVoterId()
                    )
            ) {

                Map<String, Object> error =
                        new HashMap<>();

                error.put("success", false);
                error.put("message",
                        "Voter ID already exists");

                return ResponseEntity
                        .badRequest()
                        .body(error);
            }

            Voter saved =
                    repo.save(voter);

            return ResponseEntity.ok(saved);

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
    // UPDATE VOTER — handles both numeric ID and voterId string
    // ─────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVoter(
            @PathVariable String id,
            @RequestBody Voter updated
    ) {

        try {

            Voter voter;

            // Try numeric ID first
            try {
                Long numericId = Long.parseLong(id);
                voter = repo.findById(numericId)
                        .orElseThrow(() -> new RuntimeException("Voter not found"));
            } catch (NumberFormatException e) {
                voter = repo.findByVoterId(id)
                        .orElseThrow(() -> new RuntimeException("Voter not found"));
            }

            // ✅ FIXED: Actually copy the updated fields
            if (updated.getVoterId() != null) {
                voter.setVoterId(updated.getVoterId());
            }
            if (updated.getName() != null) {
                voter.setName(updated.getName());
            }
            if (updated.getDob() != null) {
                voter.setDob(updated.getDob());
            }
            if (updated.getMobileNo() != null) {
                voter.setMobileNo(updated.getMobileNo());
            }

            Voter saved =
                    repo.save(voter);

            return ResponseEntity.ok(saved);

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
    // DELETE VOTER — handles both numeric ID and voterId string
    // ─────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVoter(
            @PathVariable String id
    ) {

        try {

            Voter voter;

            // Try numeric ID first
            try {
                Long numericId = Long.parseLong(id);
                voter = repo.findById(numericId)
                        .orElseThrow(() -> new RuntimeException("Voter not found"));
            } catch (NumberFormatException e) {
                voter = repo.findByVoterId(id)
                        .orElseThrow(() -> new RuntimeException("Voter not found"));
            }

            // Remove voter's votes from blockchain and decrement candidate counts
            if (voter.isHasVoted()) {
                blockService.removeVoterVotes(voter.getVoterId());
            }

            repo.delete(voter);

            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", true);
            response.put("message",
                    "Voter deleted");

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
    // DELETE ALL VOTERS
    // ─────────────────────────────────────────
    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllVoters() {

        try {

            repo.resetTable();
            repo.resetAutoIncrement();

            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", true);
            response.put("message",
                    "All voters deleted");

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
    // VERIFY VOTER USING voterId + DOB
    // ─────────────────────────────────────────
    @PostMapping("/verify")
    public ResponseEntity<?> verifyVoter(
            @RequestBody Map<String, String> body
    ) {

        try {

            String voterId =
                    body.get("voterId");

            String dobStr =
                    body.get("dob");

            LocalDate dob =
                    LocalDate.parse(dobStr);

            Optional<Voter> voter =
                    repo.findByVoterIdAndDob(
                            voterId,
                            dob
                    );

            if (voter.isEmpty()) {

                Map<String, Object> error =
                        new HashMap<>();

                error.put("success", false);
                error.put("message",
                        "Invalid voter credentials");

                return ResponseEntity
                        .badRequest()
                        .body(error);
            }

            return ResponseEntity.ok(
                    voter.get()
            );

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
    // SEND OTP
    // ─────────────────────────────────────────
    @PostMapping("/{voterId}/send-otp")
    public ResponseEntity<?> sendOtp(
            @PathVariable String voterId
    ) {

        try {

            String otp =
                    otpService.generateAndSendOtp(
                            voterId
                    );

            Map<String, Object> response =
                    new HashMap<>();

            response.put("success", true);
            response.put("message", "OTP sent");

            // DEV ONLY
            response.put("otp", otp);

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
    // VERIFY OTP
    // ─────────────────────────────────────────
    @PostMapping("/{voterId}/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @PathVariable String voterId,
            @RequestBody Map<String, String> body
    ) {
        try {

            String otp = body.get("otp");

            boolean valid = otpService.verifyOtp(voterId, otp);

            if (!valid) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "Invalid or expired OTP"
                        ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "OTP verified successfully"
            ));

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "message", e.getMessage()
                    ));
        }
    }
}