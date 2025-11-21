package com.backend.backend.repositories;

import com.backend.backend.entities.AdviceRequest;
import com.backend.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;

public interface AdviceRequestRepository extends JpaRepository<AdviceRequest, String> {
    // Obtener solicitudes pendientes para asesores
    List<AdviceRequest> findByStatus(AdviceRequest.Status status);
    
    // Obtener solicitudes de un usuario específico
    List<AdviceRequest> findByUser(User user);
    
    // Historial de un usuario, ordenado de lo mas nuevo a lo mas viejito
    List<AdviceRequest> findByUserOrderByCreatedAtDesc(User user);
    
    // Obtener solicitudes aceptadas por un asesor específico
    List<AdviceRequest> findByAdvisor(User advisor);
    
    // Obtener solicitudes de un usuario con un estado específico
    List<AdviceRequest> findByUserAndStatus(User user, AdviceRequest.Status status);
    
    // Obtener solicitudes aceptadas por un asesor con un estado específico
    List<AdviceRequest> findByAdvisorAndStatus(User advisor, AdviceRequest.Status status);

    // Búsqueda con filtros combinables: rango de fechas (createdAt) y nombre de usuario (firstName + lastName)
    @Query("SELECT ar FROM AdviceRequest ar JOIN ar.user u " +
            "WHERE (:start IS NULL OR ar.createdAt >= :start) " +
            "AND (:end IS NULL OR ar.createdAt <= :end) " +
            "AND (:username IS NULL OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :username, '%'))) ")
    List<AdviceRequest> findByFilters(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("username") String username);
}