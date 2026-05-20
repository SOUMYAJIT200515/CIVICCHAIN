package com.example.civicpulse.state;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VotingState {

    @Id
    private Long id;

    private String status;

    private LocalDateTime endTime;

    private int durationMinutes;
}