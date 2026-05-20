package com.example.civicpulse.controller;

import com.example.civicpulse.model.Candidate;
import com.example.civicpulse.service.CandidateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidates")
@CrossOrigin(origins = "*")
public class CandidateController {

    private final CandidateService service;

    public CandidateController(CandidateService service) {
        this.service = service;
    }

    // ─────────────────────────────
    // GET ALL
    @GetMapping
    public List<Candidate> getAll() {
        return service.getAll();
    }

    // ─────────────────────────────
    // ADD
    @PostMapping
    public Candidate add(@RequestBody Candidate c) {
        return service.save(c);
    }

    // ─────────────────────────────
    // GET ONE by numeric ID (admin frontend uses c.id)
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable String id) {
        try {
            // Try numeric ID first (admin panel uses this)
            Long numericId = Long.parseLong(id);
            return ResponseEntity.ok(service.getOneById(numericId));
        } catch (NumberFormatException e) {
            // Fall back to candidateId string (e.g. "CAND001")
            try {
                return ResponseEntity.ok(service.getById(id));
            } catch (Exception ex) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Candidate not found: " + id);
                return ResponseEntity.badRequest().body(error);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ─────────────────────────────
    // UPDATE — handles both numeric ID and candidateId string
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable String id,
            @RequestBody Candidate c
    ) {
        try {
            Candidate updated;
            try {
                Long numericId = Long.parseLong(id);
                updated = service.updateById(numericId, c);
            } catch (NumberFormatException e) {
                updated = service.update(id, c);
            }
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ─────────────────────────────
    // DELETE — handles both numeric ID and candidateId string
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            try {
                Long numericId = Long.parseLong(id);
                service.deleteById(numericId);
            } catch (NumberFormatException e) {
                service.delete(id);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Candidate deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ─────────────────────────────
    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAll() {
        service.deleteAll();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "All candidates deleted successfully");
        return ResponseEntity.ok(response);
    }
}