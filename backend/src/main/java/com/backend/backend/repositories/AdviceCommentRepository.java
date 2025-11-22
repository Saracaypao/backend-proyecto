package com.backend.backend.repositories;

import com.backend.backend.entities.AdviceComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdviceCommentRepository extends JpaRepository<AdviceComment, String> {
    List<AdviceComment> findByTransactionId(String transactionId);
}

