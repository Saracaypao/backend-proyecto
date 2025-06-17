package com.backend.backend.controllers;


import com.backend.backend.dto.TransactionDTO;
import com.backend.backend.dto.TransactionDetailsDTO;
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

    // Devuelve las ultimas 5 transacciones
    @GetMapping("/latest")
    public ResponseEntity<List<Transaction>> getLastTransaction(@AuthenticationPrincipal User user){
        try{
            List<Transaction> last5 = transactionService.getLast5Transactions(user);
            return ResponseEntity.ok(last5);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Devuelve las transacciones filtradas por categoria y fecha
    @GetMapping("/filter")
    public ResponseEntity<List<Transaction>> filterTransactions(
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String date,
            @AuthenticationPrincipal User user
    ) {
        try {
            List<Transaction> filtered = transactionService.filterTransactions(user.getEmail(), categoryId, date);
            return ResponseEntity.ok(filtered);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obtener una transaccion por su id
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDetailsDTO> getTransactionById(@PathVariable String id, @AuthenticationPrincipal User user) {
        try {
            TransactionDetailsDTO dto = transactionService.getTransactionByUser(id, user);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
