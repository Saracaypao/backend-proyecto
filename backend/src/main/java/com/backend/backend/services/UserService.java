package com.backend.backend.services;

import com.backend.backend.dto.UserRegisterDTO;
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

    // Lógica para registrar un usuario -> siempre USER
    public void registerUser(UserRegisterDTO dto) {
        if (userRepository.findByEmailIgnoreCase(dto.getEmail()).isPresent()) {
            throw new RuntimeException("This email is already in use");
        }

        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(User.Role.USER) // siempre user
                .build();

        userRepository.save(user);
    }

    // Crear un advisor desde endpoint interno
    public User createAdvisor(UserRegisterDTO dto) {
        if (userRepository.findByEmailIgnoreCase(dto.getEmail()).isPresent()) {
            throw new RuntimeException("This email is already in use");
        }

        User advisor = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(User.Role.ADVISOR) // Para advisor
                .build();

        return userRepository.save(advisor);
    }

    // Metodo para cambiar contraseña
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Incorrect password");
        }

        // Actualizar la contraseña
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
