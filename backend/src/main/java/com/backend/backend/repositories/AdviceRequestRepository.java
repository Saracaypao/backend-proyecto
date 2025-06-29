package com.backend.backend.repositories;

import com.backend.backend.entities.AdviceRequest;
import com.backend.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdviceRequestRepository extends JpaRepository<AdviceRequest, String> {
    // Obtener solicitudes pendientes para asesores
    List<AdviceRequest> findByStatus(AdviceRequest.Status status);
    
    // Obtener solicitudes de un usuario específico
    List<AdviceRequest> findByUser(User user);
    
    // Obtener solicitudes aceptadas por un asesor específico
    List<AdviceRequest> findByAdvisor(User advisor);
    
    // Obtener solicitudes de un usuario con un estado específico
    List<AdviceRequest> findByUserAndStatus(User user, AdviceRequest.Status status);
    
    // Obtener solicitudes aceptadas por un asesor con un estado específico
    List<AdviceRequest> findByAdvisorAndStatus(User advisor, AdviceRequest.Status status);
} 