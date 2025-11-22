package com.backend.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdviceHistoryItemDTO {
    // item sencillo para mostrar en el historial del perfil
    private String idAsesoria;
    private LocalDateTime fecha;   // cuando se creó o cuando tengamos fecha
    private String tipo;           // la categoria si existe, sino algo simple
    private String descripcion;    // descripcion que escribió el usuario
}
