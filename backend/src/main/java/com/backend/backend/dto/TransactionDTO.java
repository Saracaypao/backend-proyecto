package com.backend.backend.dto;

import com.backend.backend.entities.Transaction.Type;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TransactionDTO {
    private String id;

    private String description;

    private String categoryId;

    private LocalDate date;

    private double amount;

    private Type type;

    @JsonProperty("isPublic")
    private boolean isPublic;


}
