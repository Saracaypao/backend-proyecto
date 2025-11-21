package com.backend.backend.controllers;

import com.backend.backend.dto.UserRegisterDTO;
import com.backend.backend.dto.AdviceHistoryItemDTO;
import com.backend.backend.entities.User;
import com.backend.backend.services.TransactionService;
import com.backend.backend.services.UserService;
import com.backend.backend.services.AdviceRequestService;
import jakarta.validation.Valid;
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
        origins = {
                "http://localhost:5500",
                "http://127.0.0.1:5500",
                "http://localhost:5173",
                "https://pnc-proyecto-final-frontend-grupo-0-delta.vercel.app"
        },
        allowedHeaders = "*",
        allowCredentials = "true"
)
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final TransactionService transactionService;
    private final AdviceRequestService adviceRequestService;  // para traer el historial de asesorías

    @Value("${app.internal.admin-secret}")
    private String internalAdminSecret;

    // To validate if a string is a valid UUID
    private final Pattern UUID_REGEX = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    public UserController(UserService userService, TransactionService transactionService, AdviceRequestService adviceRequestService) {
        this.userService = userService;
        this.transactionService = transactionService;
        this.adviceRequestService = adviceRequestService;
    }


    // Endpoint público: registra usuario
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
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }

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

    // Desde el perfil de cada usuario: ver su historial de asesorías
    // Lista cronológica (lo mas nuevo primero) con fecha, tipo (categoria) y descripción
    @GetMapping("/{userId}/advice-history")
    public ResponseEntity<?> verHistorialDeAsesorias(@PathVariable String userId) {
        try {
            var historial = adviceRequestService.getHistorialDeAsesoriasDeUsuario(userId);

            // devolvemos tal cual la lista para que el frontend la pinte sin recargar la pagina
            return ResponseEntity.ok(historial);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    // Devuelve los datos del usuario logueado
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        Map<String, Object> body = new HashMap<>();
        body.put("firstName", user.getFirstName());
        body.put("lastName", user.getLastName());
        body.put("email", user.getEmail());
        body.put("bio", user.getBio());
        body.put("role", user.getRole().name());

        return ResponseEntity.ok(body);
    }

    // Actualiza firstName, lastName, email y bio del usuario logueado
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> payload
    ) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        String firstName = (payload.getOrDefault("firstName", "")).trim();
        String lastName  = (payload.getOrDefault("lastName", "")).trim();
        String email     = (payload.getOrDefault("email", "")).trim();
        String bio       = (payload.getOrDefault("bio", "")).trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "First name and last name are required"));
        }
        if (email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        try {
            System.out.println("Updating profile for user: " + user.getEmail());
            System.out.println("New values -> firstName=" + firstName +
                    ", lastName=" + lastName + ", email=" + email + ", bio=" + bio);

            User updated = userService.updateUserProfile(
                    user.getEmail(),
                    firstName,
                    lastName,
                    email,
                    bio
            );

            Map<String, Object> body = new HashMap<>();
            body.put("firstName", updated.getFirstName());
            body.put("lastName", updated.getLastName());
            body.put("email", updated.getEmail());
            body.put("bio", updated.getBio());
            body.put("role", updated.getRole().name());

            return ResponseEntity.ok(body);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }
    //Epica 6 Historia de usuario 5 y 6
    // Graficos de gastos públicos de un usuario (solo asesores)
    // - distribucion por categoria
    // - tendencia de gasto por fecha
    // Los datos cambian segun el rango de fechas enviado
    @GetMapping("/{userId}/public-spending/charts")
    public ResponseEntity<?> getPublicSpendingCharts(
            @PathVariable String userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @AuthenticationPrincipal User advisor
    ) {
        try {
            if (advisor == null || advisor.getRole() != User.Role.ADVISOR) {
                return ResponseEntity.status(403).body(Map.of("error", "Solo asesores pueden ver graficos de gastos públicos"));
            }

            var data = transactionService.obtenerGraficosDeGastosPublicos(userId, startDate, endDate);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
