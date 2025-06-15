package com.backend.backend.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class UserResponseDTO {
    private String id;
    private String fullName;
    private String email;
    private String role;
    private String token;
}
