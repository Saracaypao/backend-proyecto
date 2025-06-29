package com.backend.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdviceCommentDTO {
    private String id;
    private String message;
    private LocalDateTime timestamp;
    private String advisorName;
    private String transactionId;
}
