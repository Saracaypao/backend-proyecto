package com.backend.backend.services;

import com.backend.backend.dto.AdviceRequestDTO;
import com.backend.backend.dto.AdviceRequestResponseDTO;
import com.backend.backend.entities.AdviceRequest;
import com.backend.backend.entities.User;
import com.backend.backend.entities.Transaction;
import com.backend.backend.repositories.AdviceRequestRepository;
import com.backend.backend.repositories.UserRepository;
import com.backend.backend.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

@Service
public class AdviceRequestService {

    @Autowired
    private AdviceRequestRepository adviceRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    // Crear una nueva solicitud de asesoría
    public AdviceRequestResponseDTO createAdviceRequest(AdviceRequestDTO dto, String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AdviceRequest request = AdviceRequest.builder()
                .user(user)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .status(AdviceRequest.Status.PENDING)
                .build();

        AdviceRequest savedRequest = adviceRequestRepository.save(request);
        return AdviceRequestResponseDTO.fromAdviceRequest(savedRequest);
    }

    // Obtener solicitudes pendientes para asesores
    public List<AdviceRequestResponseDTO> getPendingRequests() {
        List<AdviceRequest> pendingRequests = adviceRequestRepository.findByStatus(AdviceRequest.Status.PENDING);
        return pendingRequests.stream()
                .map(AdviceRequestResponseDTO::fromAdviceRequest)
                .collect(Collectors.toList());
    }

    // Obtener solicitudes de un usuario específico
    public List<AdviceRequestResponseDTO> getUserRequests(String userEmail) {
        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<AdviceRequest> userRequests = adviceRequestRepository.findByUser(user);
        return userRequests.stream()
                .map(AdviceRequestResponseDTO::fromAdviceRequest)
                .collect(Collectors.toList());
    }

    // Obtener solicitudes aceptadas por un asesor
    public List<AdviceRequestResponseDTO> getAdvisorRequests(String advisorEmail) {
        User advisor = userRepository.findByEmailIgnoreCase(advisorEmail)
                .orElseThrow(() -> new RuntimeException("Advisor not found"));

        if (advisor.getRole() != User.Role.ADVISOR) {
            throw new RuntimeException("Only advisors can view assignments");
        }

        List<AdviceRequest> advisorRequests = adviceRequestRepository.findByAdvisor(advisor);
        return advisorRequests.stream()
                .map(AdviceRequestResponseDTO::fromAdviceRequest)
                .collect(Collectors.toList());
    }

    // Aceptar una solicitud de asesoría
    public void acceptRequest(String requestId, User advisor) {
        AdviceRequest request = adviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!advisor.getRole().equals(User.Role.ADVISOR)) {
            throw new RuntimeException("Solo los asesores pueden aceptar solicitudes");
        }

        if (!request.getStatus().equals(AdviceRequest.Status.PENDING)) {
            throw new RuntimeException("Solo se pueden aceptar solicitudes pendientes");
        }

        request.setStatus(AdviceRequest.Status.ACCEPTED);
        request.setAdvisor(advisor);
        request.setAcceptedAt(LocalDateTime.now());
        adviceRequestRepository.save(request);
    }

    // Marcar solicitud como en progreso
    public void startAdvice(String requestId, User advisor) {
        AdviceRequest request = adviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!advisor.getRole().equals(User.Role.ADVISOR)) {
            throw new RuntimeException("Solo los asesores pueden iniciar asesoría");
        }

        if (!request.getAdvisor().getId().equals(advisor.getId())) {
            throw new RuntimeException("Solo el asesor asignado puede iniciar la asesoría");
        }

        if (!request.getStatus().equals(AdviceRequest.Status.ACCEPTED)) {
            throw new RuntimeException("La solicitud debe estar aceptada antes de iniciar la asesoría");
        }

        request.setStatus(AdviceRequest.Status.IN_PROGRESS);
        adviceRequestRepository.save(request);
    }

    // Completar una solicitud de asesoría
    public void completeRequest(String requestId, User advisor) {
        AdviceRequest request = adviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!advisor.getRole().equals(User.Role.ADVISOR)) {
            throw new RuntimeException("Solo los asesores pueden completar asesoría");
        }

        if (!request.getAdvisor().getId().equals(advisor.getId())) {
            throw new RuntimeException("Solo el asesor asignado puede completar la asesoría");
        }

        if (!request.getStatus().equals(AdviceRequest.Status.IN_PROGRESS)) {
            throw new RuntimeException("La solicitud debe estar en progreso antes de completarla");
        }

        request.setStatus(AdviceRequest.Status.COMPLETED);
        request.setCompletedAt(LocalDateTime.now());
        adviceRequestRepository.save(request);
    }

    // Cancelar una solicitud
    public AdviceRequestResponseDTO cancelRequest(String requestId, String userEmail) {
        AdviceRequest request = adviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        User user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!request.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Only the request owner can cancel the request");
        }

        if (request.getStatus() == AdviceRequest.Status.COMPLETED) {
            throw new RuntimeException("Cannot cancel completed request");
        }

        request.setStatus(AdviceRequest.Status.CANCELLED);
        request.setUpdatedAt(LocalDateTime.now());

        AdviceRequest savedRequest = adviceRequestRepository.save(request);
        return AdviceRequestResponseDTO.fromAdviceRequest(savedRequest);
    }

    public void rejectRequest(String requestId, User advisor) {
        AdviceRequest request = adviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        
        if (!advisor.getRole().equals(User.Role.ADVISOR)) {
            throw new RuntimeException("Solo los asesores pueden rechazar solicitudes");
        }
        
        if (!request.getStatus().equals(AdviceRequest.Status.PENDING)) {
            throw new RuntimeException("Solo se pueden rechazar solicitudes pendientes");
        }
        
        request.setStatus(AdviceRequest.Status.CANCELLED);
        request.setAdvisor(advisor);
        adviceRequestRepository.save(request);
    }

    // Obtener reporte de una solicitud (para asesores)
    public Map<String, Object> getReport(String requestId, User advisor) {
        // Verificar que el usuario sea asesor
        if (advisor == null || !advisor.getRole().equals(User.Role.ADVISOR)) {
            throw new RuntimeException("Solo los asesores pueden ver reportes");
        }
        
        // Buscar la solicitud
        AdviceRequest request = adviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // Generar reporte básico
        Map<String, Object> report = new HashMap<>();
        report.put("requestId", request.getId());
        report.put("title", request.getTitle() != null ? request.getTitle() : "Sin título");
        report.put("description", request.getDescription() != null ? request.getDescription() : "Sin descripción");
        report.put("startDate", request.getStartDate() != null ? request.getStartDate().toString() : "No especificada");
        report.put("endDate", request.getEndDate() != null ? request.getEndDate().toString() : "No especificada");
        report.put("status", request.getStatus().toString());
        report.put("userEmail", request.getUser().getEmail());
        report.put("userName", request.getUser().getFirstName() + " " + request.getUser().getLastName());
        report.put("advisorEmail", advisor.getEmail());
        report.put("advisorRole", advisor.getRole().toString());
        report.put("message", "Reporte generado exitosamente para el período solicitado");
        report.put("timestamp", System.currentTimeMillis());
        
        // Obtener transacciones públicas del usuario en el período solicitado
        List<Transaction> publicTransactions = transactionRepository.findByUserAndIsPublicTrueAndDateBetween(
            request.getUser(),
            request.getStartDate(),
            request.getEndDate()
        );
        
        // Calcular métricas de las transacciones
        double totalIncome = publicTransactions.stream()
            .filter(t -> t.getType() == Transaction.Type.INCOME)
            .mapToDouble(Transaction::getAmount)
            .sum();
            
        double totalExpense = publicTransactions.stream()
            .filter(t -> t.getType() == Transaction.Type.EXPENSE)
            .mapToDouble(Transaction::getAmount)
            .sum();
            
        double balance = totalIncome - totalExpense;
        
        // Formatear transacciones para el reporte
        List<Map<String, Object>> formattedTransactions = publicTransactions.stream()
            .map(t -> {
                Map<String, Object> transaction = new HashMap<>();
                transaction.put("id", t.getId());
                transaction.put("description", t.getDescription());
                transaction.put("amount", t.getAmount());
                transaction.put("type", t.getType().toString());
                transaction.put("category", t.getCategory() != null ? t.getCategory().getName() : "Sin categoría");
                transaction.put("date", t.getDate().toString());
                return transaction;
            })
            .collect(Collectors.toList());
        
        // Agregar métricas y transacciones al reporte
        report.put("transactions", formattedTransactions);
        report.put("totalTransactions", formattedTransactions.size());
        report.put("totalIncome", totalIncome);
        report.put("totalExpense", totalExpense);
        report.put("balance", balance);
        report.put("period", request.getStartDate() + " a " + request.getEndDate());
        
        return report;
    }

    // Proporcionar asesoría (para asesores)
    public void provideAdvice(String requestId, String advice, User advisor) {
        AdviceRequest request = adviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!advisor.getRole().equals(User.Role.ADVISOR)) {
            throw new RuntimeException("Solo los asesores pueden proporcionar asesoría");
        }

        if (!request.getAdvisor().getId().equals(advisor.getId())) {
            throw new RuntimeException("Solo el asesor asignado puede proporcionar asesoría");
        }

        if (!request.getStatus().equals(AdviceRequest.Status.IN_PROGRESS)) {
            throw new RuntimeException("La solicitud debe estar en progreso para proporcionar asesoría");
        }

        // Guardar el mensaje de asesoría en la solicitud
        request.setAdviceMessage(advice);
        request.setAdviceProvidedAt(LocalDateTime.now());
        request.setStatus(AdviceRequest.Status.COMPLETED);
        request.setCompletedAt(LocalDateTime.now());
        adviceRequestRepository.save(request);
    }
} 