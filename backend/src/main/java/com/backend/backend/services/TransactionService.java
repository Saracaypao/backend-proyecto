package com.backend.backend.services;

import com.backend.backend.dto.AdviceCommentDTO;
import com.backend.backend.dto.TransactionDTO;
import com.backend.backend.dto.TransactionDetailsDTO;
import com.backend.backend.entities.Category;
import com.backend.backend.entities.Transaction;
import com.backend.backend.entities.User;
import com.backend.backend.repositories.AdviceCommentRepository;
import com.backend.backend.repositories.AdviceRequestRepository;
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

    @Autowired
    private AdviceRequestRepository adviceRequestRepository;

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

    // Eliminado: funcionalidad de gráficos para el advisor

    // Pie chart de transacciones públicas (todas las cuentas), agrupadas por categoría y sumando montos.
    // Si startDate y endDate son nulos, devuelve TODO el historial desde el comienzo.
    public Map<String, Object> getPublicTransactionsPie(LocalDate startDate, LocalDate endDate) {
        List<Transaction> source;

        if (startDate == null && endDate == null) {
            // Todo el historial
            source = transactionRepository.findByIsPublicTrue();
        } else {
            // Normalizar rangos parciales
            LocalDate start = (startDate != null) ? startDate : LocalDate.of(1900, 1, 1);
            LocalDate end = (endDate != null) ? endDate : LocalDate.now();
            if (end.isBefore(start)) {
                LocalDate tmp = start;
                start = end;
                end = tmp;
            }
            source = transactionRepository.findByIsPublicTrueAndDateBetween(start, end);
        }

        // Agrupar por categoría y sumar montos (incluye ingresos y gastos tal cual están)
        Map<String, Double> distribution = new LinkedHashMap<>();
        for (Transaction t : source) {
            String categoryName = (t.getCategory() != null && t.getCategory().getName() != null && !t.getCategory().getName().isEmpty())
                    ? t.getCategory().getName()
                    : "Sin categoría";
            distribution.put(categoryName, distribution.getOrDefault(categoryName, 0.0) + t.getAmount());
        }

        double total = source.stream().mapToDouble(Transaction::getAmount).sum();

        Map<String, Object> result = new HashMap<>();
        result.put("total", BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP).doubleValue());
        result.put("items", source.size());
        result.put("distributionByCategory", distribution);
        return result;
    }

    // Distribución por categoría de transacciones públicas de un cliente específico (para asesores)
    // Si no se envían fechas, devuelve el historial completo público del cliente.
    public Map<String, Object> getPublicByClientCategory(User advisor, String clientUserId, LocalDate startDate, LocalDate endDate) {
        if (advisor == null || advisor.getRole() != User.Role.ADVISOR) {
            throw new RuntimeException("Solo asesores pueden acceder a esta información");
        }

        User client = userRepository.findById(clientUserId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Validar que el cliente esté asignado al asesor en al menos una solicitud (histórica o actual)
        boolean assigned = adviceRequestRepository.findByAdvisor(advisor)
                .stream().anyMatch(ar -> ar.getUser() != null && ar.getUser().getId().equals(client.getId()));
        if (!assigned) {
            throw new RuntimeException("El cliente no está asignado al asesor");
        }

        List<Transaction> source;
        if (startDate == null && endDate == null) {
            source = transactionRepository.findByUserAndIsPublicTrue(client);
        } else {
            LocalDate start = (startDate != null) ? startDate : LocalDate.of(1900, 1, 1);
            LocalDate end = (endDate != null) ? endDate : LocalDate.now();
            if (end.isBefore(start)) {
                LocalDate tmp = start;
                start = end;
                end = tmp;
            }
            source = transactionRepository.findByUserAndIsPublicTrueAndDateBetween(client, start, end);
        }

        Map<String, Double> distribution = new LinkedHashMap<>();
        for (Transaction t : source) {
            String categoryName = (t.getCategory() != null && t.getCategory().getName() != null && !t.getCategory().getName().isEmpty())
                    ? t.getCategory().getName()
                    : "Sin categoría";
            distribution.put(categoryName, distribution.getOrDefault(categoryName, 0.0) + t.getAmount());
        }

        double total = source.stream().mapToDouble(Transaction::getAmount).sum();

        Map<String, Object> result = new HashMap<>();
        result.put("total", BigDecimal.valueOf(total).setScale(2, RoundingMode.HALF_UP).doubleValue());
        result.put("items", source.size());
        result.put("distributionByCategory", distribution);
        result.put("categories", new ArrayList<>(distribution.keySet()));
        result.put("data", new ArrayList<>(distribution.values()));
        return result;
    }

    // Dataset por defecto: devolver todas las categorías existentes con valores en 0
    public Map<String, Object> getDefaultZeroByCategory() {
        List<Category> categories = categoryRepository.findAll();

        List<String> names = new ArrayList<>();
        List<Double> zeros = new ArrayList<>();

        for (Category c : categories) {
            String name = (c != null && c.getName() != null && !c.getName().isBlank()) ? c.getName() : "Sin categoría";
            names.add(name);
            zeros.add(0.0);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("categories", names);
        result.put("data", zeros);
        result.put("total", 0.0);
        result.put("items", 0);
        result.put("distributionByCategory", new LinkedHashMap<>());
        return result;
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


     //  Obtener gastos diarios
public Map<String, Object> getDaily(User user) {
    LocalDate today = LocalDate.now();

    List<Transaction> transactions = transactionRepository.findByUserIdAndDate(
            user.getId(),
            today
    );

    double dailyExpense = transactions.stream()
            .filter(t -> t.getType() == Transaction.Type.EXPENSE)
            .mapToDouble(Transaction::getAmount)
            .average()
            .orElse(0.0);

    Map<String, Object> result = new HashMap<>();
    result.put("averageDailyExpense", BigDecimal.valueOf(dailyExpense)
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue());

    return result;
}

// Obtenr gastos semanales
public Map<String, Object> getWeekly(User user) {
    LocalDate end = LocalDate.now();
    LocalDate start = end.minusDays(6); // incluye hoy

    List<Transaction> transactions = transactionRepository.findByUserIdAndDateBetween(
            user.getId(),
            start,
            end
    );

    double totalExpense = transactions.stream()
            .filter(t -> t.getType() == Transaction.Type.EXPENSE)
            .mapToDouble(Transaction::getAmount)
            .sum();

    double weeklyAverage = totalExpense / 7.0;

    Map<String, Object> result = new HashMap<>();
    result.put("averageWeeklyExpense", BigDecimal.valueOf(weeklyAverage)
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue());

    return result;
}

}
