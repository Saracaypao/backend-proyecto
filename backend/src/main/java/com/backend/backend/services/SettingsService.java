package com.backend.backend.services;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SettingsService {

    // Por ahora usamos un Map en memoria, en producción esto debería estar en base de datos
    private final Map<String, Map<String, Object>> userSettings = new HashMap<>();

    public Map<String, Object> getUserSettings(String email) {
        return userSettings.getOrDefault(email, getDefaultSettings());
    }

    public Map<String, Object> updateUserSettings(String email, Map<String, Object> newSettings) {
        Map<String, Object> currentSettings = getUserSettings(email);
        currentSettings.putAll(newSettings);
        userSettings.put(email, currentSettings);
        return currentSettings;
    }

    private Map<String, Object> getDefaultSettings() {
        Map<String, Object> defaultSettings = new HashMap<>();
        defaultSettings.put("currency", "USD");
        defaultSettings.put("language", "es");
        defaultSettings.put("notifications", true);
        defaultSettings.put("theme", "light");
        defaultSettings.put("timezone", "America/Mexico_City");
        return defaultSettings;
    }
} 