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
    private String userName;
    private String advisorName;
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
                .userName(request.getUser() != null ? 
                    request.getUser().getFirstName() + " " + request.getUser().getLastName() : 
                    "Usuario")
                .advisorName(request.getAdvisor() != null ? 
                    request.getAdvisor().getFirstName() + " " + request.getAdvisor().getLastName() : 
                    null)
                .acceptedAt(request.getAcceptedAt())
                .completedAt(request.getCompletedAt())
                .adviceMessage(request.getAdviceMessage())
                .adviceProvidedAt(request.getAdviceProvidedAt())
                .build();
    }
} 