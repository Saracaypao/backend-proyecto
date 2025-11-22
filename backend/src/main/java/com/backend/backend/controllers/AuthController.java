package com.backend.backend.controllers;

import com.backend.backend.dto.UserLoginDTO;
import com.backend.backend.dto.UserResponseDTO;
import com.backend.backend.entities.User;
import com.backend.backend.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.Map;

@CrossOrigin(
        origins = "https://pnc-proyecto-final-frontend-grupo-0-delta.vercel.app",
        allowedHeaders = "*",
        allowCredentials = "true"
)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private AuthService authService;

    //Maneja peticion para logearse
    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@RequestBody UserLoginDTO dto) {
        try {
            return ResponseEntity.ok(authService.login(dto));
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    // Maneja petición para cerrar sesión
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        try {
            // En una implementación real, podrías invalidar el token aquí
            // Por ahora, solo retornamos éxito ya que el frontend maneja la limpieza
            return ResponseEntity.ok().body(Map.of("message", "Logout exitoso"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Verificar si el token es válido
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@AuthenticationPrincipal User user) {
        try {
            if (user != null) {
                return ResponseEntity.ok().body(Map.of(
                    "valid", true,
                    "user", Map.of(
                        "id", user.getId(),
                        "email", user.getEmail(),
                        "role", user.getRole().toString()
                    )
                ));
            } else {
                return ResponseEntity.status(401).body(Map.of("valid", false));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("valid", false));
        }
    }
}