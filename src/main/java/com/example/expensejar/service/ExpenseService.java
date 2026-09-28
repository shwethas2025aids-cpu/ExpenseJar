package com.example.expensejar.service;

import com.example.expensejar.dto.BudgetAlertResponse;
import com.example.expensejar.dto.CategorySummaryResponse;
import com.example.expensejar.dto.ExpenseRequest;
import com.example.expensejar.dto.ExpenseResponse;
import com.example.expensejar.dto.MonthlyTrendResponse;
import com.example.expensejar.entity.Budget;
import com.example.expensejar.entity.Category;
import com.example.expensejar.entity.Expense;
import com.example.expensejar.entity.User;
import com.example.expensejar.exception.InvalidExpenseException;
import com.example.expensejar.exception.ResourceNotFoundException;
import com.example.expensejar.repository.BudgetRepository;
import com.example.expensejar.repository.CategoryRepository;
import com.example.expensejar.repository.ExpenseRepository;
import com.example.expensejar.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExpenseService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;

    public ExpenseService(ExpenseRepository expenseRepository,
                          UserRepository userRepository,
                          CategoryRepository categoryRepository,
                          BudgetRepository budgetRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.budgetRepository = budgetRepository;
    }

    /**
     * Adds a new expense, enforcing:
     * RULE 1: Expense amount must be strictly positive.
     * RULE 2: Budget alerts are recalculated whenever a new expense is added.
     */
    public BudgetAlertResponse addExpense(ExpenseRequest request) {
        // Enforce RULE 1: Positive expense amount
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new InvalidExpenseException("Expense amount must be greater than zero");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        // Save the valid expense
        Expense expense = new Expense(request.getAmount(), request.getDate(), user, category);
        expenseRepository.save(expense);

        // Enforce RULE 2: Recalculate budget alert for this category and month
        YearMonth ym = YearMonth.from(expense.getDate());
        LocalDate startOfMonth = ym.atDay(1);
        LocalDate endOfMonth = ym.atEndOfMonth();
        String monthStr = ym.format(MONTH_FORMATTER);

        Double totalSpent = expenseRepository.calculateTotalSpentByCategoryAndDateRange(
                user.getId(), category.getId(), startOfMonth, endOfMonth);
        if (totalSpent == null) {
            totalSpent = expense.getAmount();
        }
        totalSpent = Math.round(totalSpent * 100.0) / 100.0;

        Optional<Budget> budgetOpt = budgetRepository.findByUserIdAndCategoryIdAndMonth(
                user.getId(), category.getId(), monthStr);

        if (budgetOpt.isPresent()) {
            Budget budget = budgetOpt.get();
            double budgetAmount = budget.getAmount();
            double percentageUsed = budgetAmount > 0 ? (totalSpent / budgetAmount) * 100.0 : 0.0;
            percentageUsed = Math.round(percentageUsed * 100.0) / 100.0;

            boolean alert = percentageUsed >= 90.0;
            String message = alert ? "Budget alert" : "Expense added successfully";

            return new BudgetAlertResponse(
                    message,
                    category.getName(),
                    totalSpent,
                    budgetAmount,
                    percentageUsed,
                    alert
            );
        } else {
            // No budget set for this category and month yet
            return new BudgetAlertResponse(
                    "Expense added successfully",
                    category.getName(),
                    totalSpent,
                    0.0,
                    0.0,
                    false
            );
        }
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAllExpenses() {
        return expenseRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        return mapToResponse(expense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return expenseRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByCategoryId(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }
        return expenseRepository.findByCategoryId(categoryId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * View total spend per category for the current month.
     */
    @Transactional(readOnly = true)
    public List<CategorySummaryResponse> getCurrentMonthCategorySummary(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        return expenseRepository.findCategorySummaryForDateRange(userId, startDate, endDate);
    }

    /**
     * View a month-over-month spending trend calculated from actual MySQL records.
     */
    @Transactional(readOnly = true)
    public List<MonthlyTrendResponse> getMonthOverMonthTrend(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        List<Expense> expenses = expenseRepository.findByUserIdOrderByDateAsc(userId);
        Map<String, Double> monthlyTotals = new TreeMap<>();

        for (Expense expense : expenses) {
            String month = expense.getDate().format(MONTH_FORMATTER);
            monthlyTotals.put(month, monthlyTotals.getOrDefault(month, 0.0) + expense.getAmount());
        }

        List<MonthlyTrendResponse> trendResponses = new ArrayList<>();
        for (Map.Entry<String, Double> entry : monthlyTotals.entrySet()) {
            trendResponses.add(new MonthlyTrendResponse(entry.getKey(), entry.getValue()));
        }

        return trendResponses;
    }

    public void deleteExpense(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));
        expenseRepository.delete(expense);
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getAmount(),
                expense.getDate(),
                expense.getUser().getId(),
                expense.getUser().getName(),
                expense.getCategory().getId(),
                expense.getCategory().getName()
        );
    }
}
