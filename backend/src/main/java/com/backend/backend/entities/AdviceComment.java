package com.backend.backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "advice_comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdviceComment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    private String message;

    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "advisor_id")
    private User advisor;
}
