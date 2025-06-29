package com.backend.backend.services;

import com.backend.backend.dto.UserRegisterDTO;
import com.backend.backend.dto.UserResponseDTO;
import com.backend.backend.entities.User;
import com.backend.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Logica para registrar un usuario
    public void registerUser(UserRegisterDTO dto) {
        if (userRepository.findByEmailIgnoreCase(dto.getEmail()).isPresent()) {
            throw new RuntimeException("This email is already in use");
        }

        // Validar y convertir el rol
        User.Role role;
        try {
            role = User.Role.valueOf(dto.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role. Must be 'USER' or 'ADVISOR'");
        }

        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);
    }
}
