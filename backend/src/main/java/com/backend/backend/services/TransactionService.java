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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

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

    public List<Transaction> getUserTransactions(String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        return transactionRepository.findByUserId(user.getId());
    }

    // Para Epica 6 Historia 6. empieza
    // Datos para graficos de gastos PUBLICOS por usuario y por rango de fechas
    // Devuelve distribucion por categoria y tendencia de gasto (por dia)
    public Map<String, Object> obtenerGraficosDeGastosPublicos(String userId, String startDateStr, String endDateStr) {
        // validamos usuario
        User usuario = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado para graficos"));

        // fechas opcionales, si no mandan usamos ultimos 30 dias
        LocalDate hoy = LocalDate.now();
        LocalDate fechaInicio = (startDateStr != null && !startDateStr.trim().isEmpty())
                ? LocalDate.parse(startDateStr)
                : hoy.minusDays(29);

        LocalDate fechaFin = (endDateStr != null && !endDateStr.trim().isEmpty())
                ? LocalDate.parse(endDateStr)
                : hoy;

        if (fechaFin.isBefore(fechaInicio)) {
            // si se van de cabeza, invertimos simple para no romper
            LocalDate tmp = fechaInicio;
            fechaInicio = fechaFin;
            fechaFin = tmp;
        }

        // traemos SOLO transacciones publicas del usuario y en el rango de fechas
        List<Transaction> transPublicas = transactionRepository
                .findByUserAndIsPublicTrueAndDateBetween(usuario, fechaInicio, fechaFin);

        // nos quedamos con gastos (EXPENSE) nada mas para estos graficos
        List<Transaction> gastos = new ArrayList<>();
        for (Transaction t : transPublicas) {
            if (t.getType() == Transaction.Type.EXPENSE) {
                gastos.add(t);
            }
        }

        // Distribucion por categoria
        Map<String, Double> distribucion = new LinkedHashMap<>();
        for (Transaction g : gastos) {
            String nombreCat = (g.getCategory() != null && g.getCategory().getName() != null && !g.getCategory().getName().isEmpty())
                    ? g.getCategory().getName()
                    : "Sin categoría";
            distribucion.put(nombreCat, distribucion.getOrDefault(nombreCat, 0.0) + g.getAmount());
        }

        // Tendencia por fecha (sumatoria diaria)
        // inicializamos todos los dias en 0 para que el grafico no tenga huecos
        Map<LocalDate, Double> tendenciaMapa = new LinkedHashMap<>();
        LocalDate cursor = fechaInicio;
        while (!cursor.isAfter(fechaFin)) {
            tendenciaMapa.put(cursor, 0.0);
            cursor = cursor.plusDays(1);
        }
        for (Transaction g : gastos) {
            LocalDate d = g.getDate();
            tendenciaMapa.put(d, tendenciaMapa.getOrDefault(d, 0.0) + g.getAmount());
        }

        // formateamos tendencia a una lista para el frontend
        List<Map<String, Object>> tendencia = new ArrayList<>();
        for (Map.Entry<LocalDate, Double> e : tendenciaMapa.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", e.getKey().toString());
            // redondeamos a 2 decimales por prolijidad
            item.put("expense", BigDecimal.valueOf(e.getValue()).setScale(2, RoundingMode.HALF_UP).doubleValue());
            tendencia.add(item);
        }

        // tambien devolvemos totales por si los quieren mostrar
        double totalGasto = gastos.stream().mapToDouble(Transaction::getAmount).sum();

        Map<String, Object> res = new HashMap<>();
        res.put("userId", usuario.getId());
        res.put("periodo", fechaInicio + " a " + fechaFin);
        res.put("totalGasto", BigDecimal.valueOf(totalGasto).setScale(2, RoundingMode.HALF_UP).doubleValue());
        res.put("distribucionPorCategoria", distribucion); // {"Alimentos": 120.5, ...}
        res.put("tendenciaPorFecha", tendencia);           // [{date:"2025-11-01", expense: 40.0}, ...]

        return res;
    }
    //Para Epica 6 Historia 6. Termina

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

    // Calcula el total de gastos e ingresos del ultimo mes
    public Map<String, Double> getCurrentMonthSummary(User user) {
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end = now.withDayOfMonth(now.lengthOfMonth());

        List<Transaction> transactions = transactionRepository.findByUserIdAndDateBetween(user.getId(), start, end);

        double incomeRaw = transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.INCOME)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double expenseRaw = transactions.stream()
                .filter(t -> t.getType() == Transaction.Type.EXPENSE)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double income = BigDecimal.valueOf(incomeRaw).setScale(2, RoundingMode.HALF_UP).doubleValue();
        double expense = BigDecimal.valueOf(expenseRaw).setScale(2, RoundingMode.HALF_UP).doubleValue();

        Map<String, Double> result = new HashMap<>();
        result.put("income", income);
        result.put("expense", expense);
        return result;
    }

    // Genera un resumen mensual de gastos e ingresos del usuario para los últimos 6 meses
    public List<Map<String, Object>> getSixMonthAnalytics(User user) {
        LocalDate now = LocalDate.now();
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            LocalDate start = month.withDayOfMonth(1);
            LocalDate end = month.withDayOfMonth(month.lengthOfMonth());

            List<Transaction> transactions = transactionRepository.findByUserIdAndDateBetween(user.getId(), start, end);

            double incomeRaw = transactions.stream()
                    .filter(t -> t.getType() == Transaction.Type.INCOME)
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            double expenseRaw = transactions.stream()
                    .filter(t -> t.getType() == Transaction.Type.EXPENSE)
                    .mapToDouble(Transaction::getAmount)
                    .sum();

            double income = BigDecimal.valueOf(incomeRaw).setScale(2, RoundingMode.HALF_UP).doubleValue();
            double expense = BigDecimal.valueOf(expenseRaw).setScale(2, RoundingMode.HALF_UP).doubleValue();

            Map<String, Object> data = new HashMap<>();
            data.put("month", month.getMonth().toString());
            data.put("income", income);
            data.put("expense", expense);
            result.add(data);
        }

        return result;
    }

    // Obtener transacciones por rango de fechas
    public List<Transaction> getTransactionsByDateRange(String email, String startDate, String endDate) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        
        return transactionRepository.findByUserIdAndDateBetween(user.getId(), start, end);
    }

    // Obtener todas las transacciones públicas para asesores financieros
    public List<Transaction> getPublicTransactions() {
        return transactionRepository.findByIsPublicTrue();
    }
}
