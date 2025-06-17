package com.backend.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdviceCommentDTO {
    private String message;
    private String advisorName;
    private LocalDateTime timestamp;
}
