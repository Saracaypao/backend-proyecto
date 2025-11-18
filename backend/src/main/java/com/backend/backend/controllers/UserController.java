package com.backend.backend.controllers;

import com.backend.backend.dto.UserRegisterDTO;
import com.backend.backend.entities.User;
import com.backend.backend.services.TransactionService;
import com.backend.backend.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@CrossOrigin(
        origins = "https://pnc-proyecto-final-frontend-grupo-0-delta.vercel.app",
        allowedHeaders = "*",
        allowCredentials = "true"
)
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    // Para validar si un string es un UUID válido
    @Value("${app.internal.admin-secret}")
    private String internalAdminSecret;

    // To validate if a string is a valid UUID
    private final Pattern UUID_REGEX = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    //  Endpoint público: registra usuario
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid UserRegisterDTO dto) {
        try {
            userService.registerUser(dto);
            return ResponseEntity.ok().body(Map.of("message", "User registered successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Internal server error"));
        }
    }

    // Endpoint interno: crear advisor
    @PostMapping("/admin/create-advisor")
    public ResponseEntity<?> createAdvisor(
            @RequestHeader(name = "X-ADMIN-SECRET", required = false) String adminSecret,
            @RequestBody @Valid UserRegisterDTO dto) {
        try {
            if (adminSecret == null || !adminSecret.equals(internalAdminSecret)) {
                return ResponseEntity.status(403).body(
                        Map.of("error", "Not authorized to create advisors")
                );
            }

            User advisor = userService.createAdvisor(dto);

            Map<String, Object> body = new HashMap<>();
            body.put("message", "Advisor created successfully");
            body.put("id", advisor.getId());
            body.put("email", advisor.getEmail());
            body.put("role", advisor.getRole().name());

            return ResponseEntity.ok(body);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Internal server error"));
        }
    }

    // Manejo de errores de validación de @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    // Endpoint para cambiar contraseña
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal User user) {
        try {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");

            if (currentPassword == null || newPassword == null) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Current password and new password are required")
                );
            }

            if (newPassword.length() < 6) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "New password must be at least 6 characters long")
                );
            }

            userService.changePassword(user.getEmail(), currentPassword, newPassword);
            return ResponseEntity.ok().body(Map.of("message", "Password changed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Internal server error"));
        }
    }
}
