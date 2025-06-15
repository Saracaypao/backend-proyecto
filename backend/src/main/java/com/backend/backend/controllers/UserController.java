package com.backend.backend.controllers;

import com.backend.backend.dto.UserRegisterDTO;
import com.backend.backend.dto.UserResponseDTO;
import com.backend.backend.entities.User;
import com.backend.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody UserRegisterDTO dto) {
        userService.registerUser(dto);
        return ResponseEntity.ok().build();
    }
}