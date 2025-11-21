package com.backend.backend.services;

import com.backend.backend.dto.AdviceRequestDTO;
import com.backend.backend.dto.AdviceRequestResponseDTO;
import com.backend.backend.dto.AdviceHistoryItemDTO;
import com.backend.backend.entities.AdviceRequest;
import com.backend.backend.entities.User;
import com.backend.backend.entities.Category;
import com.backend.backend.entities.Transaction;
import com.backend.backend.repositories.AdviceRequestRepository;
import com.backend.backend.repositories.UserRepository;
import com.backend.backend.repositories.TransactionRepository;
import com.backend.backend.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
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

    @Autowired
    private CategoryRepository categoryRepository;

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

        // Epica 6 Hitoria 4. comienza
        // intentar asignar categoria si viene en el dto (puede venir vacia y no pasa nada)
        if (dto.getCategoryId() != null && !dto.getCategoryId().trim().isEmpty()) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Categoria no encontrada, revise el id porfa"));
            request.setCategory(category);
        }        // Epica 6 Hitoria 4. termina

        AdviceRequest savedRequest = adviceRequestRepository.save(request);
        return AdviceRequestResponseDTO.fromAdviceRequest(savedRequest);
    }

    //Epica 6 Histsoria 4. Comienza
    // Listado con filtros combinables por rango de fechas (creación) y nombre del usuario
    public List<AdviceRequestResponseDTO> listAdviceRequests(LocalDate startDate, LocalDate endDate, String username) {
        LocalDateTime start = null;
        LocalDateTime end = null;

        if (startDate != null) {
            start = startDate.atStartOfDay();
        }
        if (endDate != null) {
            // incluir todo el día hasta 23:59:59.999999999
            end = endDate.atTime(23, 59, 59, 999_000_000);
        }

        // normalizar username vacío a null para que el repositorio lo ignore
        String normalizedUsername = (username != null && !username.trim().isEmpty()) ? username.trim() : null;

        List<AdviceRequest> results = adviceRequestRepository.findByFilters(start, end, normalizedUsername);
        return results.stream()
                .map(AdviceRequestResponseDTO::fromAdviceRequest)
                .collect(Collectors.toList());
    }

    // Obtener solicitudes pendientes para asesores
    public List<AdviceRequestResponseDTO> getPendingRequests() {
        List<AdviceRequest> pendingRequests = adviceRequestRepository.findByStatus(AdviceRequest.Status.PENDING);
        return pendingRequests.stream()
                .map(AdviceRequestResponseDTO::fromAdviceRequest)
                .collect(Collectors.toList());
    }    //Epica 6 Histsoria 4. Termina

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
        if (request.getCategory() != null) {
            report.put("categoryId", request.getCategory().getId());
            report.put("categoryName", request.getCategory().getName());
        } else {
            report.put("categoryId", null);
            report.put("categoryName", "Sin categoría");
        }
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

    // Epica 6 Historia 4. comienza
    // Editar solo la categoria de una asesoría, pensado para el asesor asignado
    public AdviceRequestResponseDTO updateCategory(String requestId, String categoryId, User advisor) {
        AdviceRequest request = adviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        // Solo asesores pueden editar y además debe ser el asignado si hay uno
        if (advisor == null || !advisor.getRole().equals(User.Role.ADVISOR)) {
            throw new RuntimeException("Solo asesores pueden editar la categoría");
        }

        if (request.getAdvisor() != null && !advisor.getId().equals(request.getAdvisor().getId())) {
            throw new RuntimeException("Solo el asesor asignado puede cambiar la categoría");
        }

        // si viene vacio, quitamos la categoria para dejarlo sin clasificacion
        if (categoryId == null || categoryId.trim().isEmpty()) {
            request.setCategory(null);
        } else {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Categoria no existe, no se puede asignar"));
            request.setCategory(category);
        }

        request.setUpdatedAt(LocalDateTime.now());
        AdviceRequest saved = adviceRequestRepository.save(request);
        return AdviceRequestResponseDTO.fromAdviceRequest(saved);
    }

    // Historial para mostrar en el perfil de un usuario, bien sencillito y entendible
    public List<AdviceHistoryItemDTO> getHistorialDeAsesoriasDeUsuario(String userId) {
        // primero buscamos al usuario, por si mandan cualquier cosa
        User elUsuario = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado para ver su historial"));

        // traemos sus asesorías ordenadas de lo más nuevo a lo más viejito
        List<AdviceRequest> asesorias = adviceRequestRepository.findByUserOrderByCreatedAtDesc(elUsuario);

        // armamos una lista con la info puntual que quiere ver el asesor: fecha, tipo y descripción
        return asesorias.stream().map(ar -> {
            String tipoCategoria = (ar.getCategory() != null && ar.getCategory().getName() != null)
                    ? ar.getCategory().getName()
                    : "Sin tipo";

            String descri = (ar.getDescription() != null && !ar.getDescription().trim().isEmpty())
                    ? ar.getDescription()
                    : "Sin descripción";

            // usamos createdAt como referencia cronológica, es cuando se registró la asesoría
            LocalDateTime fechaQueMostramos = ar.getCreatedAt();

            return AdviceHistoryItemDTO.builder()
                    .idAsesoria(ar.getId())
                    .fecha(fechaQueMostramos)
                    .tipo(tipoCategoria)
                    .descripcion(descri)
                    .build();
        }).collect(Collectors.toList());
    }// EPICA 6 HISTORIA 4. TERMINA
}