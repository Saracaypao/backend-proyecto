package com.backend.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRegisterDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
}
