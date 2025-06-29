package com.backend.backend.services;

import com.backend.backend.dto.AdviceCommentRequestDTO;
import com.backend.backend.dto.AdviceCommentDTO;
import com.backend.backend.entities.AdviceComment;
import com.backend.backend.entities.Transaction;
import com.backend.backend.entities.User;
import com.backend.backend.repositories.AdviceCommentRepository;
import com.backend.backend.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdviceCommentService {

    @Autowired
    private AdviceCommentRepository adviceCommentRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    // Logica para crear un comentario
    public void createComment(AdviceCommentRequestDTO request, User advisor) {
        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.isPublic()) {
            throw new RuntimeException("Cannot comment on a private transaction");
        }

        AdviceComment comment = AdviceComment.builder()
                .message(request.getMessage())
                .transaction(transaction)
                .timestamp(LocalDateTime.now())
                .advisor(advisor)
                .build();

        adviceCommentRepository.save(comment);
    }

    // Obtener comentarios de una transacción específica
    public List<AdviceCommentDTO> getCommentsByTransaction(String transactionId) {
        List<AdviceComment> comments = adviceCommentRepository.findByTransactionId(transactionId);
        
        return comments.stream()
                .map(comment -> AdviceCommentDTO.builder()
                        .id(comment.getId())
                        .message(comment.getMessage())
                        .timestamp(comment.getTimestamp())
                        .advisorName(comment.getAdvisor().getFirstName() + " " + comment.getAdvisor().getLastName())
                        .transactionId(comment.getTransaction().getId())
                        .build())
                .collect(Collectors.toList());
    }
}
