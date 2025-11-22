package com.backend.backend.controllers;

import com.backend.backend.entities.User;
import com.backend.backend.services.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(
        origins = "https://pnc-proyecto-final-frontend-grupo-0-delta.vercel.app",
        allowedHeaders = "*",
        allowCredentials = "true"
)
@RestController
@RequestMapping("/settings")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    // Obtener configuraciones del usuario
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSettings(@AuthenticationPrincipal User user) {
        try {
            Map<String, Object> settings = settingsService.getUserSettings(user.getEmail());
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Actualizar configuraciones del usuario
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateSettings(
            @RequestBody Map<String, Object> newSettings,
            @AuthenticationPrincipal User user) {
        try {
            Map<String, Object> updatedSettings = settingsService.updateUserSettings(user.getEmail(), newSettings);
            return ResponseEntity.ok(updatedSettings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 