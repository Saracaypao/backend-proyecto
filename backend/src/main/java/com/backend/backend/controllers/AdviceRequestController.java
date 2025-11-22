package com.backend.backend.controllers;

import com.backend.backend.dto.AdviceRequestDTO;
import com.backend.backend.dto.AdviceRequestResponseDTO;
import com.backend.backend.dto.AdvisorAssignedClientDTO;
import com.backend.backend.entities.AdviceRequest;
import com.backend.backend.entities.User;
import com.backend.backend.services.AdviceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@CrossOrigin(
        origins = {
                // Dev ports usados por distintas herramientas
                "http://localhost:3001",
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                // Vite y Live Server
                "http://localhost:5173",
                "http://localhost:5500",
                "http://127.0.0.1:5500",
                // Deploys en Vercel conocidos
                "https://pnc-proyecto-final-frontend-grupo-0-five.vercel.app",
                "https://pnc-proyecto-final-frontend-grupo-0-delta.vercel.app"
        },
        allowedHeaders = "*",
        allowCredentials = "true"
)
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

    // Buscar asesorías con filtros combinables por rango de fechas y nombre de usuario
    @GetMapping("/search")
    public ResponseEntity<List<AdviceRequestResponseDTO>> search(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String username
    ) {
        try {
            List<AdviceRequestResponseDTO> results = adviceRequestService.listAdviceRequests(startDate, endDate, username);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
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

    // Obtener asignaciones del asesor actual filtradas por estado (por defecto, ACCEPTED)
    @GetMapping("/my-assignments")
    public ResponseEntity<List<AdviceRequestResponseDTO>> getMyAssignments(
            @AuthenticationPrincipal User advisor,
            @RequestParam(required = false) AdviceRequest.Status status
    ) {
        try {
            // Si no se especifica un estado, devolver TODAS las asesorías asignadas al asesor.
            // Esto evita que la vista principal aparezca vacía cuando las asesorías no están en estado ACCEPTED.
            List<AdviceRequestResponseDTO> requests = (status == null)
                    ? adviceRequestService.getAdvisorRequests(advisor.getEmail())
                    : adviceRequestService.getAdvisorRequestsByStatus(advisor.getEmail(), status);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Nueva pestaña: En progreso -> solo solicitudes IN_PROGRESS del asesor autenticado
    @GetMapping("/in-progress")
    public ResponseEntity<List<AdviceRequestResponseDTO>> getInProgressAssignments(@AuthenticationPrincipal User advisor) {
        try {
            List<AdviceRequestResponseDTO> requests = adviceRequestService.getAdvisorRequestsByStatus(advisor.getEmail(), AdviceRequest.Status.IN_PROGRESS);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Nueva pestaña: Completadas -> solo solicitudes COMPLETED del asesor autenticado
    @GetMapping("/completed")
    public ResponseEntity<List<AdviceRequestResponseDTO>> getCompletedAssignments(@AuthenticationPrincipal User advisor) {
        try {
            List<AdviceRequestResponseDTO> requests = adviceRequestService.getAdvisorRequestsByStatus(advisor.getEmail(), AdviceRequest.Status.COMPLETED);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Buscar mis asignaciones (asesor) con filtros por fecha y usuario, sin recargar página
    @GetMapping("/my-assignments/search")
    public ResponseEntity<List<AdviceRequestResponseDTO>> searchMyAssignments(
            @AuthenticationPrincipal User advisor,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String username
    ) {
        try {
            List<AdviceRequestResponseDTO> results = adviceRequestService
                    .listAdvisorAssignments(advisor, startDate, endDate, username);
            return ResponseEntity.ok(results);
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

    // Actualizar solo la categoría de una asesoría (pensado para el asesor)
    @PutMapping("/{id}/category")
    public ResponseEntity<?> updateCategory(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal User advisor
    ) {
        try {
            String categoryId = body != null ? body.get("categoryId") : null;

            AdviceRequestResponseDTO updated = adviceRequestService.updateCategory(id, categoryId, advisor);

            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Listado de clientes asignados históricamente al asesor (únicos, cualquier estado)
    @GetMapping("/my-assigned-clients")
    public ResponseEntity<?> getMyAssignedClients(@AuthenticationPrincipal User advisor) {
        try {
            if (advisor == null || advisor.getRole() != User.Role.ADVISOR) {
                return ResponseEntity.status(403).body(Map.of("error", "Solo asesores"));
            }
            List<AdvisorAssignedClientDTO> clients = adviceRequestService.getAssignedClients(advisor);
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}