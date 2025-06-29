package com.backend.backend.controllers;


import com.backend.backend.dto.TransactionDTO;
import com.backend.backend.dto.TransactionDetailsDTO;
import com.backend.backend.dto.TransactionResponseDTO;
import com.backend.backend.entities.Transaction;
import com.backend.backend.entities.User;
import com.backend.backend.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Void> createTransaction(@RequestBody TransactionDTO dto, @AuthenticationPrincipal User user) {
        try {
            transactionService.createTransaction(dto, user.getEmail());
            return ResponseEntity.ok().build();
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> getMyTransactions(@AuthenticationPrincipal User user) {
        try {
            List<Transaction> transactions = transactionService.getUserTransactions(user.getEmail());
            
            List<TransactionResponseDTO> responseDTOs = transactions.stream()
                    .map(TransactionResponseDTO::fromTransaction)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(responseDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<List<Transaction>> getLastTransaction(@AuthenticationPrincipal User user){
        try{
            List<Transaction> last5 = transactionService.getLast5Transactions(user);
            return ResponseEntity.ok(last5);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Transaction>> filterTransactions(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Transaction.Type type,
            @AuthenticationPrincipal User user
    ) {
        try {
            List<Transaction> filtered = transactionService.filterTransactions(user.getEmail(), categoryId, date, type);
            return ResponseEntity.ok(filtered);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDetailsDTO> getTransactionById(@PathVariable String id, @AuthenticationPrincipal User user) {
        try {
            TransactionDetailsDTO dto = transactionService.getTransactionByUser(id, user);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateTransaction(
            @PathVariable String id,
            @RequestBody TransactionDTO dto,
            @AuthenticationPrincipal User user
    ) {
        try {
            transactionService.updateTransaction(id, dto, user.getEmail());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable String id,
            @AuthenticationPrincipal User user
    ) {
        try {
            transactionService.deleteTransaction(id, user.getEmail());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/summary/current-month")
    public ResponseEntity<Map<String, Double>> getMonthlySummary(@AuthenticationPrincipal User user) {
        try {
            Map<String, Double> summary = transactionService.getCurrentMonthSummary(user);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/summary/last-6-months")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyBreakdown(@AuthenticationPrincipal User user) {
        try {
            List<Map<String, Object>> data = transactionService.getSixMonthAnalytics(user);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/public")
    public ResponseEntity<List<TransactionResponseDTO>> getPublicTransactions() {
        try {
            List<Transaction> publicTransactions = transactionService.getPublicTransactions();
            List<TransactionResponseDTO> responseDTOs = publicTransactions.stream()
                    .map(TransactionResponseDTO::fromTransaction)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
