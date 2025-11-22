package com.backend.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AdviceRequestDTO {
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;

    // id de la categoria elegida para clasificar la asesoría, puede venir vacio
    private String categoryId;
}