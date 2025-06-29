package com.backend.backend.controllers;

import com.backend.backend.dto.UserRegisterDTO;
import com.backend.backend.dto.UserResponseDTO;
import com.backend.backend.entities.Transaction;
import com.backend.backend.entities.User;
import com.backend.backend.services.TransactionService;
import com.backend.backend.services.UserService;
import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    //Para validar si un string es un UUID valido
    private final Pattern UUID_REGEX = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    //Maneja peticion para registrar un usuario
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid UserRegisterDTO dto) {
        try{
            userService.registerUser(dto);
            return ResponseEntity.ok().body(Map.of("message", "Usuario registrado exitosamente"));
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", "Error interno del servidor"));
        }
    }

    // Manejo de errores de validación
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
}