package com.backend.backend.controllers;

import com.backend.backend.dto.AdviceCommentRequestDTO;
import com.backend.backend.dto.AdviceCommentDTO;
import com.backend.backend.entities.User;
import com.backend.backend.services.AdviceCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(
        origins = "https://pnc-proyecto-final-frontend-grupo-0-delta.vercel.app",
        allowedHeaders = "*",
        allowCredentials = "true"
)

@RestController
@RequestMapping("/comments")
public class AdviceCommentController {

    @Autowired
    private AdviceCommentService adviceCommentService;

    // Maneja peticion para crear un comentario
    @PostMapping
    public ResponseEntity<Void> createComment(@RequestBody AdviceCommentRequestDTO request, @AuthenticationPrincipal User advisor) {
        try {
            adviceCommentService.createComment(request, advisor);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obtener comentarios de una transacción específica
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<List<AdviceCommentDTO>> getCommentsByTransaction(@PathVariable String transactionId) {
        try {
            List<AdviceCommentDTO> comments = adviceCommentService.getCommentsByTransaction(transactionId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
