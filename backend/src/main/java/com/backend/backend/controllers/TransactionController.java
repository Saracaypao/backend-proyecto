package com.backend.backend.controllers;


import com.backend.backend.dto.TransactionDTO;
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

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // Maneja peticion para crear una transaccion
    @PostMapping
    public ResponseEntity<Void> createTransaction(@RequestBody TransactionDTO dto, @AuthenticationPrincipal User user) {
        try {
            transactionService.createTransaction(dto, user.getEmail());
            return ResponseEntity.ok().build();
        } catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    // Devuelve las transacciones del usuario que esta logueado
    @GetMapping
    public ResponseEntity<List<Transaction>> getMyTransactions(@AuthenticationPrincipal User user) {
        try {
            List<Transaction> transactions = transactionService.getUserTransactions(user.getEmail());
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
