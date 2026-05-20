package com.example.civicpulse.service;

import com.example.civicpulse.model.Candidate;
import com.example.civicpulse.repo.CandidateRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandidateService {

    private final CandidateRepository repo;

    public CandidateService(CandidateRepository repo) {
        this.repo = repo;
    }

    // ─────────────────────────────
    public List<Candidate> getAll() {
        return repo.findAll();
    }

    // ─────────────────────────────
    public Candidate save(Candidate c) {
        return repo.save(c);
    }

    // ─────────────────────────────
    // UPDATE by numeric ID (used by admin frontend)
    public Candidate updateById(Long id, Candidate c) {

        Candidate existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found with id: " + id));

        existing.setName(c.getName());
        existing.setParty(c.getParty());
        existing.setCandidateId(c.getCandidateId());
        existing.setEmoji(c.getEmoji());
        existing.setColor(c.getColor());
        existing.setTag(c.getTag());

        return repo.save(existing);
    }

    // ─────────────────────────────
    // UPDATE by candidateId string (e.g. "CAND001")
    public Candidate update(String candidateId, Candidate c) {

        Candidate existing = repo.findByCandidateId(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        existing.setName(c.getName());
        existing.setParty(c.getParty());
        existing.setEmoji(c.getEmoji());
        existing.setColor(c.getColor());
        existing.setTag(c.getTag());

        return repo.save(existing);
    }

    // ─────────────────────────────
    // DELETE by numeric ID
    public void deleteById(Long id) {

        Candidate existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found with id: " + id));

        repo.delete(existing);
    }

    // ─────────────────────────────
    // DELETE by candidateId string
    public void delete(String candidateId) {

        Candidate existing = repo.findByCandidateId(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found"));

        repo.delete(existing);
    }

    // ─────────────────────────────
    // GET ONE by numeric ID
    public Candidate getOneById(Long id) {

        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found with id: " + id));
    }

    // ─────────────────────────────
    // GET ONE by candidateId string
    public Candidate getById(String candidateId) {

        return repo.findByCandidateId(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate not found with id: " + candidateId));
    }

    // ─────────────────────────────
    @Transactional
    public void deleteAll() {
        repo.deleteAll();
    }
}