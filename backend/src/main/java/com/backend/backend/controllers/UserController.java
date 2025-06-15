package com.backend.backend.controllers;

import com.backend.backend.dto.UserRegisterDTO;
import com.backend.backend.dto.UserResponseDTO;
import com.backend.backend.entities.User;
import com.backend.backend.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    //Para validar si un string es un UUID valido
    private final Pattern UUID_REGEX = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    //Maneja peticion para registrar un usuario
    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@RequestBody @Valid UserRegisterDTO dto) {
        try{
            userService.registerUser(dto);
        return ResponseEntity.ok().build();
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }

    }
}