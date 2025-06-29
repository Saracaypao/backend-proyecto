package com.backend.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "advice_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdviceRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advisor_id")
    private User advisor;

    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private String adviceMessage;
    private LocalDateTime adviceProvidedAt;

    public enum Status {
        PENDING,    // Pendiente de ser aceptada por un asesor
        ACCEPTED,   // Aceptada por un asesor
        IN_PROGRESS, // En proceso de asesoría
        COMPLETED,  // Completada
        CANCELLED   // Cancelada
    }
} 