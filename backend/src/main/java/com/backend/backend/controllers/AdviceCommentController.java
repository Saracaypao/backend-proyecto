package com.backend.backend.controllers;

import com.backend.backend.dto.AdviceCommentRequestDTO;
import com.backend.backend.entities.User;
import com.backend.backend.services.AdviceCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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



}
