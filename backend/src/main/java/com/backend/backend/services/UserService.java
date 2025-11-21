package com.backend.backend.services;

import com.backend.backend.dto.UserRegisterDTO;
import com.backend.backend.entities.User;
import com.backend.backend.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Lógica para registrar un usuario -> siempre USER
    @Transactional
    public void registerUser(UserRegisterDTO dto) {
        if (userRepository.findByEmailIgnoreCase(dto.getEmail()).isPresent()) {
            throw new RuntimeException("This email is already in use");
        }

        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(User.Role.USER) // siempre USER
                .build();

        userRepository.save(user);
    }

    // Crear un advisor desde endpoint interno
    @Transactional
    public User createAdvisor(UserRegisterDTO dto) {
        if (userRepository.findByEmailIgnoreCase(dto.getEmail()).isPresent()) {
            throw new RuntimeException("This email is already in use");
        }

        User advisor = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(User.Role.ADVISOR)
                .build();

        return userRepository.save(advisor);
    }

    // Cambiar contraseña
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Incorrect password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Actualizar perfil (nombre, email, bio)
    @Transactional
    public User updateUserProfile(
            String currentEmail,
            String firstName,
            String lastName,
            String newEmail,
            String bio
    ) {
        // 1) Buscar usuario por el email actual
        User user = userRepository.findByEmailIgnoreCase(currentEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2) Si cambia de email, validar que no esté usado por otro
        if (!currentEmail.equalsIgnoreCase(newEmail)
                && userRepository.findByEmailIgnoreCase(newEmail).isPresent()) {
            throw new RuntimeException("This email is already in use");
        }

        // 3) Actualizar campos
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(newEmail);
        user.setBio(bio);

        System.out.println("Saving user with id=" + user.getId());

        // 4) Guardar en BD
        return userRepository.save(user);
    }
}
