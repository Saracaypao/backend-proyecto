package com.backend.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class TransactionDetailsDTO {
    private String id;
    private String description;
    private LocalDate date;
    private double amount;
    private String type;
    private String category;
    private List<AdviceCommentDTO> comments;
}
