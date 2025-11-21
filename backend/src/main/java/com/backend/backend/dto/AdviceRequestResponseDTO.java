package com.backend.backend.dto;

import com.backend.backend.entities.AdviceRequest;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class AdviceRequestResponseDTO {
    private String id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private String status;
    // datos del usuario solicitante
    private String userId;
    private String userEmail;
    private String userName;
    // datos del asesor (si existe)
    private String advisorId;
    private String advisorEmail;
    private String advisorName;
    // info basica de la categoria para mostrar en historial/reportes
    private String categoryId;
    private String categoryName;
    private LocalDateTime acceptedAt;
    private LocalDateTime completedAt;
    private String adviceMessage;
    private LocalDateTime adviceProvidedAt;

    public static AdviceRequestResponseDTO fromAdviceRequest(AdviceRequest request) {
        return AdviceRequestResponseDTO.builder()
                .id(request.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdAt(request.getCreatedAt())
                .status(request.getStatus().name())
                .userId(request.getUser() != null ? request.getUser().getId() : null)
                .userEmail(request.getUser() != null ? request.getUser().getEmail() : null)
                .userName(request.getUser() != null ? 
                    request.getUser().getFirstName() + " " + request.getUser().getLastName() : 
                    "Usuario")
                .advisorId(request.getAdvisor() != null ? request.getAdvisor().getId() : null)
                .advisorEmail(request.getAdvisor() != null ? request.getAdvisor().getEmail() : null)
                .advisorName(request.getAdvisor() != null ? 
                    request.getAdvisor().getFirstName() + " " + request.getAdvisor().getLastName() : 
                    null)
                .categoryId(request.getCategory() != null ? request.getCategory().getId() : null)
                .categoryName(request.getCategory() != null ? request.getCategory().getName() : null)
                .acceptedAt(request.getAcceptedAt())
                .completedAt(request.getCompletedAt())
                .adviceMessage(request.getAdviceMessage())
                .adviceProvidedAt(request.getAdviceProvidedAt())
                .build();
    }
}