package com.backend.backend.controllers;

import com.backend.backend.dto.AdviceRequestDTO;
import com.backend.backend.dto.AdviceRequestResponseDTO;
import com.backend.backend.entities.User;
import com.backend.backend.services.AdviceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/advice-requests")
public class AdviceRequestController {

    @Autowired
    private AdviceRequestService adviceRequestService;

    // Obtener reporte de una solicitud (para asesores)
    @GetMapping("/{id}/get-report")
    public ResponseEntity<?> getReport(@PathVariable String id, @AuthenticationPrincipal User advisor) {
        try {
            Map<String, Object> report = adviceRequestService.getReport(id, advisor);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Crear una nueva solicitud de asesoría
    @PostMapping
    public ResponseEntity<AdviceRequestResponseDTO> createRequest(
            @RequestBody AdviceRequestDTO dto,
            @AuthenticationPrincipal User user) {
        try {
            AdviceRequestResponseDTO response = adviceRequestService.createAdviceRequest(dto, user.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obtener solicitudes pendientes (para asesores)
    @GetMapping("/pending")
    public ResponseEntity<List<AdviceRequestResponseDTO>> getPendingRequests() {
        try {
            List<AdviceRequestResponseDTO> requests = adviceRequestService.getPendingRequests();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obtener solicitudes del usuario actual
    @GetMapping("/my-requests")
    public ResponseEntity<List<AdviceRequestResponseDTO>> getMyRequests(@AuthenticationPrincipal User user) {
        try {
            List<AdviceRequestResponseDTO> requests = adviceRequestService.getUserRequests(user.getEmail());
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obtener solicitudes aceptadas por el asesor actual
    @GetMapping("/my-assignments")
    public ResponseEntity<List<AdviceRequestResponseDTO>> getMyAssignments(@AuthenticationPrincipal User advisor) {
        try {
            List<AdviceRequestResponseDTO> requests = adviceRequestService.getAdvisorRequests(advisor.getEmail());
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Aceptar una solicitud de asesoría
    @PostMapping("/{id}/accept")
    public ResponseEntity<?> acceptRequest(@PathVariable String id, @AuthenticationPrincipal User advisor) {
        try {
            adviceRequestService.acceptRequest(id, advisor);
            return ResponseEntity.ok().body(Map.of("message", "Solicitud aceptada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Rechazar una solicitud de asesoría
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable String id, @AuthenticationPrincipal User advisor) {
        try {
            adviceRequestService.rejectRequest(id, advisor);
            return ResponseEntity.ok().body(Map.of("message", "Solicitud rechazada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Iniciar asesoría
    @PostMapping("/{id}/start")
    public ResponseEntity<?> startAdvice(@PathVariable String id, @AuthenticationPrincipal User advisor) {
        try {
            adviceRequestService.startAdvice(id, advisor);
            return ResponseEntity.ok().body(Map.of("message", "Asesoría iniciada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Completar asesoría
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completeRequest(@PathVariable String id, @AuthenticationPrincipal User advisor) {
        try {
            adviceRequestService.completeRequest(id, advisor);
            return ResponseEntity.ok().body(Map.of("message", "Solicitud completada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Cancelar solicitud
    @PostMapping("/{requestId}/cancel")
    public ResponseEntity<AdviceRequestResponseDTO> cancelRequest(
            @PathVariable String requestId,
            @AuthenticationPrincipal User user) {
        try {
            AdviceRequestResponseDTO response = adviceRequestService.cancelRequest(requestId, user.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Proporcionar asesoría (para asesores)
    @PostMapping("/{id}/advice")
    public ResponseEntity<?> provideAdvice(
            @PathVariable String id, 
            @RequestBody Map<String, String> adviceData,
            @AuthenticationPrincipal User advisor) {
        try {
            String advice = adviceData.get("advice");
            if (advice == null || advice.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "La asesoría no puede estar vacía"));
            }
            adviceRequestService.provideAdvice(id, advice, advisor);
            return ResponseEntity.ok().body(Map.of("message", "Asesoría proporcionada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 