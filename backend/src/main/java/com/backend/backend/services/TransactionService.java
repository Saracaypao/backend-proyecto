package com.backend.backend.services;

import com.backend.backend.dto.AdviceCommentDTO;
import com.backend.backend.dto.TransactionDTO;
import com.backend.backend.dto.TransactionDetailsDTO;
import com.backend.backend.entities.Category;
import com.backend.backend.entities.Transaction;
import com.backend.backend.entities.User;
import com.backend.backend.repositories.AdviceCommentRepository;
import com.backend.backend.repositories.CategoryRepository;
import com.backend.backend.repositories.TransactionRepository;
import com.backend.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AdviceCommentRepository adviceCommentRepository;


    // Logica para crear una nueva transaccion
    public void createTransaction(TransactionDTO dto, String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow();

        Transaction transaction = Transaction.builder()
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .date(dto.getDate())
                .type(dto.getType())
                .isPublic(dto.isPublic())
                .user(user)
                .category(category)
                .build();

        transactionRepository.save(transaction);
    }

    // Busca al usuario por su email y devuelve todas sus transacciones
    public List<Transaction> getUserTransactions(String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        return transactionRepository.findByUserId(user.getId());
    }

    // Devuelve las ultimas 5 transacciones del usuario
    public List<Transaction> getLast5Transactions(User user) {
        return transactionRepository.findTop5ByUserOrderByDateDesc(user);
    }

    // Devuelve las transacciones filtradas por categoria, fecha, tipo o categoria y fecha
    public List<Transaction> filterTransactions(String email, String categoryId, String dateString, Transaction.Type type) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        final LocalDate date = (dateString != null && !dateString.isEmpty()) ? LocalDate.parse(dateString) : null;

        final String finalCategoryId = categoryId;
        final LocalDate finalDate = date;
        final Transaction.Type finalType = type;

        if (categoryId != null && date != null && type != null) {
            return transactionRepository.findByUserId(user.getId()).stream()
                    .filter(t -> t.getCategory().getId().equals(finalCategoryId)
                            && t.getDate().equals(finalDate)
                            && t.getType().equals(finalType))
                    .toList();
        } else if (categoryId != null && date != null) {
            return transactionRepository.findByUserIdAndCategoryIdAndDate(user.getId(), categoryId, date);
        } else if (categoryId != null) {
            return transactionRepository.findByUserIdAndCategoryId(user.getId(), categoryId);
        } else if (date != null) {
            return transactionRepository.findByUserIdAndDate(user.getId(), date);
        } else if (type != null) {
            return transactionRepository.findByUserIdAndType(user.getId(), type);
        } else {
            return transactionRepository.findByUserId(user.getId());
        }
    }

    // Devuelve el detalle de una transaccion
    public TransactionDetailsDTO getTransactionByUser(String id, User user) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        List<AdviceCommentDTO> comments = List.of();
        // Solo se pueden comentar las transacciones publicas
        if (transaction.isPublic()) {
            comments = adviceCommentRepository.findByTransactionId(id)
                    .stream()
                    .map(comment -> AdviceCommentDTO.builder()
                            .message(comment.getMessage())
                            .timestamp(comment.getTimestamp())
                            .advisorName(comment.getAdvisor().getFirstName() + " " + comment.getAdvisor().getLastName())
                            .build())
                    .toList();
        }

        return TransactionDetailsDTO.builder()
                .id(transaction.getId())
                .description(transaction.getDescription())
                .date(transaction.getDate())
                .amount(transaction.getAmount())
                .type(transaction.getType().name())
                .category(transaction.getCategory().getName())
                .comments(comments)
                .build();
    }

    // Editar una transacción
    public void updateTransaction(String id, TransactionDTO dto, String email) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Unauthorized access");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        transaction.setDescription(dto.getDescription());
        transaction.setAmount(dto.getAmount());
        transaction.setDate(dto.getDate());
        transaction.setType(dto.getType());
        transaction.setPublic(dto.isPublic());
        transaction.setCategory(category);

        transactionRepository.save(transaction);
    }

    // Eliminar una transacción
    public void deleteTransaction(String id, String email) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!transaction.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Unauthorized access");
        }

        transactionRepository.delete(transaction);
    }
}
