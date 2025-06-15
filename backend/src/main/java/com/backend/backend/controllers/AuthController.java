package com.backend.backend.controllers;

import com.backend.backend.dto.UserLoginDTO;
import com.backend.backend.dto.UserResponseDTO;
import com.backend.backend.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}