package com.backend.backend.dto;

import com.backend.backend.entities.Transaction;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TransactionResponseDTO {
    private String id;
    private String description;
    private String categoryName;
    private LocalDate date;
    private double amount;
    private String type;
    
    @JsonProperty("isPublic")
    private boolean isPublic;
    
    private String userName;
    
    public static TransactionResponseDTO fromTransaction(Transaction transaction) {
        return TransactionResponseDTO.builder()
                .id(transaction.getId())
                .description(transaction.getDescription())
                .categoryName(transaction.getCategory() != null ? transaction.getCategory().getName() : "Sin categoría")
                .date(transaction.getDate())
                .amount(transaction.getAmount())
                .type(transaction.getType().name())
                .isPublic(transaction.isPublic())
                .userName(transaction.getUser() != null ? 
                    transaction.getUser().getFirstName() + " " + transaction.getUser().getLastName() : 
                    "Usuario")
                .build();
    }
} 