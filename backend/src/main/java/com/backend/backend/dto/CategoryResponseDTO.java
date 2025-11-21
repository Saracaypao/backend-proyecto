package com.backend.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponseDTO {
    private String id;
    private String name;
}
