package com.backend.backend.controllers;

import com.backend.backend.entities.User;
import com.backend.backend.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(
        origins = "https://pnc-proyecto-final-frontend-grupo-0-delta.vercel.app",
        allowedHeaders = "*",
        allowCredentials = "true"
)
@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private TransactionService transactionService;

    // Obtener reporte general
    @GetMapping
    public ResponseEntity<Map<String, Object>> getGeneralReport(@AuthenticationPrincipal User user) {
        try {
            List<com.backend.backend.entities.Transaction> transactions = 
                transactionService.getUserTransactions(user.getEmail());
            
            double totalIncome = transactions.stream()
                .filter(t -> t.getType() == com.backend.backend.entities.Transaction.Type.INCOME)
                .mapToDouble(com.backend.backend.entities.Transaction::getAmount)
                .sum();
                
            double totalExpenses = transactions.stream()
                .filter(t -> t.getType() == com.backend.backend.entities.Transaction.Type.EXPENSE)
                .mapToDouble(com.backend.backend.entities.Transaction::getAmount)
                .sum();
            
            double balance = totalIncome - totalExpenses;
            
            Map<String, Object> report = new HashMap<>();
            report.put("totalIncome", BigDecimal.valueOf(totalIncome).setScale(2, RoundingMode.HALF_UP).doubleValue());
            report.put("totalExpenses", BigDecimal.valueOf(totalExpenses).setScale(2, RoundingMode.HALF_UP).doubleValue());
            report.put("balance", BigDecimal.valueOf(balance).setScale(2, RoundingMode.HALF_UP).doubleValue());
            report.put("period", "General");
            report.put("currency", "USD");
            report.put("totalTransactions", transactions.size());
            
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obtener reporte por año
    @GetMapping("/{year}")
    public ResponseEntity<Map<String, Object>> getReportByYear(
            @PathVariable int year,
            @AuthenticationPrincipal User user) {
        try {
            LocalDate start = LocalDate.of(year, 1, 1);
            LocalDate end = LocalDate.of(year, 12, 31);
            
            List<com.backend.backend.entities.Transaction> transactions = 
                transactionService.getTransactionsByDateRange(user.getEmail(), start.toString(), end.toString());
            
            double totalIncome = transactions.stream()
                .filter(t -> t.getType() == com.backend.backend.entities.Transaction.Type.INCOME)
                .mapToDouble(com.backend.backend.entities.Transaction::getAmount)
                .sum();
                
            double totalExpenses = transactions.stream()
                .filter(t -> t.getType() == com.backend.backend.entities.Transaction.Type.EXPENSE)
                .mapToDouble(com.backend.backend.entities.Transaction::getAmount)
                .sum();
            
            double balance = totalIncome - totalExpenses;
            
            Map<String, Object> report = new HashMap<>();
            report.put("year", year);
            report.put("totalIncome", BigDecimal.valueOf(totalIncome).setScale(2, RoundingMode.HALF_UP).doubleValue());
            report.put("totalExpenses", BigDecimal.valueOf(totalExpenses).setScale(2, RoundingMode.HALF_UP).doubleValue());
            report.put("balance", BigDecimal.valueOf(balance).setScale(2, RoundingMode.HALF_UP).doubleValue());
            report.put("period", "Year " + year);
            report.put("currency", "USD");
            report.put("totalTransactions", transactions.size());
            
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obtener reporte por mes
    @GetMapping("/{year}/{month}")
    public ResponseEntity<Map<String, Object>> getReportByMonth(
            @PathVariable int year, 
            @PathVariable int month,
            @AuthenticationPrincipal User user) {
        try {
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
            
            List<com.backend.backend.entities.Transaction> transactions = 
                transactionService.getTransactionsByDateRange(user.getEmail(), start.toString(), end.toString());
            
            double totalIncome = transactions.stream()
                .filter(t -> t.getType() == com.backend.backend.entities.Transaction.Type.INCOME)
                .mapToDouble(com.backend.backend.entities.Transaction::getAmount)
                .sum();
                
            double totalExpenses = transactions.stream()
                .filter(t -> t.getType() == com.backend.backend.entities.Transaction.Type.EXPENSE)
                .mapToDouble(com.backend.backend.entities.Transaction::getAmount)
                .sum();
            
            double balance = totalIncome - totalExpenses;
            
            Map<String, Object> report = new HashMap<>();
            report.put("year", year);
            report.put("month", month);
            report.put("totalIncome", BigDecimal.valueOf(totalIncome).setScale(2, RoundingMode.HALF_UP).doubleValue());
            report.put("totalExpenses", BigDecimal.valueOf(totalExpenses).setScale(2, RoundingMode.HALF_UP).doubleValue());
            report.put("balance", BigDecimal.valueOf(balance).setScale(2, RoundingMode.HALF_UP).doubleValue());
            report.put("period", String.format("%d/%02d", year, month));
            report.put("currency", "USD");
            report.put("totalTransactions", transactions.size());
            
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obtener reporte por rango de fechas (semanal)
    @GetMapping("/range")
    public ResponseEntity<Map<String, Object>> getReportByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @AuthenticationPrincipal User user) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(startDate, formatter);
            LocalDate end = LocalDate.parse(endDate, formatter);
            
            List<com.backend.backend.entities.Transaction> transactions = 
                transactionService.getTransactionsByDateRange(user.getEmail(), startDate, endDate);
            
            double totalIncome = transactions.stream()
                .filter(t -> t.getType() == com.backend.backend.entities.Transaction.Type.INCOME)
                .mapToDouble(com.backend.backend.entities.Transaction::getAmount)
                .sum();
                
            double totalExpenses = transactions.stream()
                .filter(t -> t.getType() == com.backend.backend.entities.Transaction.Type.EXPENSE)
                .mapToDouble(com.backend.backend.entities.Transaction::getAmount)
                .sum();
            
            double balance = totalIncome - totalExpenses;
            
            Map<String, Object> report = new HashMap<>();
            report.put("startDate", startDate);
            report.put("endDate", endDate);
            report.put("totalIncome", BigDecimal.valueOf(totalIncome).setScale(2, RoundingMode.HALF_UP).doubleValue());
            report.put("totalExpenses", BigDecimal.valueOf(totalExpenses).setScale(2, RoundingMode.HALF_UP).doubleValue());
            report.put("balance", BigDecimal.valueOf(balance).setScale(2, RoundingMode.HALF_UP).doubleValue());
            report.put("period", "Weekly Report");
            report.put("currency", "USD");
            report.put("daysInRange", end.toEpochDay() - start.toEpochDay() + 1);
            report.put("totalTransactions", transactions.size());
            
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obtener datos de los últimos 6 meses
    @GetMapping("/last6months")
    public ResponseEntity<List<Map<String, Object>>> getLast6MonthsData(@AuthenticationPrincipal User user) {
        try {
            List<Map<String, Object>> monthlyData = new ArrayList<>();
            LocalDate currentDate = LocalDate.now();
            
            for (int i = 5; i >= 0; i--) {
                LocalDate monthStart = currentDate.minusMonths(i).withDayOfMonth(1);
                LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
                
                List<com.backend.backend.entities.Transaction> transactions = 
                    transactionService.getTransactionsByDateRange(user.getEmail(), monthStart.toString(), monthEnd.toString());
                
                double income = transactions.stream()
                    .filter(t -> t.getType() == com.backend.backend.entities.Transaction.Type.INCOME)
                    .mapToDouble(com.backend.backend.entities.Transaction::getAmount)
                    .sum();
                    
                double expenses = transactions.stream()
                    .filter(t -> t.getType() == com.backend.backend.entities.Transaction.Type.EXPENSE)
                    .mapToDouble(com.backend.backend.entities.Transaction::getAmount)
                    .sum();
                
                Map<String, Object> monthData = new HashMap<>();
                monthData.put("month", monthStart.getMonth().toString());
                monthData.put("income", BigDecimal.valueOf(income).setScale(2, RoundingMode.HALF_UP).doubleValue());
                monthData.put("expenses", BigDecimal.valueOf(expenses).setScale(2, RoundingMode.HALF_UP).doubleValue());
                
                monthlyData.add(monthData);
            }
            
            return ResponseEntity.ok(monthlyData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 