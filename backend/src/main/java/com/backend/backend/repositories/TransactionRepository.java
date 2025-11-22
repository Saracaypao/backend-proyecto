package com.backend.backend.repositories;

import com.backend.backend.entities.Transaction;
import com.backend.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByUserId(String userId);
    List<Transaction> findTop5ByUserOrderByDateDesc(User user);
    List<Transaction> findByUser(User user);
    List<Transaction> findByUserIdAndCategoryId(String userId, String CategoryId);
    List<Transaction> findByUserIdAndDate(String userId, LocalDate date);
    List<Transaction> findByUserIdAndCategoryIdAndDate(String userId, String CategoryId, LocalDate date);
    List<Transaction> findByUserIdAndType(String userId, Transaction.Type type);
    List<Transaction> findByUserIdAndDateBetween(String userId, LocalDate startDate, LocalDate endDate);
    List<Transaction> findByIsPublicTrue();
    List<Transaction> findByIsPublicTrueAndDateBetween(LocalDate startDate, LocalDate endDate);
    List<Transaction> findByUserAndIsPublicTrue(User user);
    List<Transaction> findByUserAndIsPublicTrueAndDateBetween(User user, LocalDate startDate, LocalDate endDate);
    void deleteByUser(User user);
}
