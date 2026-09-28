package com.example.expensejar.controller;

import com.example.expensejar.dto.BudgetAlertResponse;
import com.example.expensejar.dto.CategorySummaryResponse;
import com.example.expensejar.dto.ExpenseRequest;
import com.example.expensejar.dto.ExpenseResponse;
import com.example.expensejar.dto.MonthlyTrendResponse;
import com.example.expensejar.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    /**
     * Adds an expense, enforces positive amount, recalculates budget alert,
     * and returns budget alert status with HTTP 201 CREATED.
     */
    @PostMapping
    public ResponseEntity<BudgetAlertResponse> addExpense(@Valid @RequestBody ExpenseRequest request) {
        BudgetAlertResponse response = expenseService.addExpense(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses() {
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(expenseService.getExpensesByUserId(userId));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByCategoryId(@PathVariable Long categoryId) {
        return ResponseEntity.ok(expenseService.getExpensesByCategoryId(categoryId));
    }

    /**
     * View total spend per category for the current month.
     */
    @GetMapping("/summary/current-month/{userId}")
    public ResponseEntity<List<CategorySummaryResponse>> getCurrentMonthCategorySummary(@PathVariable Long userId) {
        return ResponseEntity.ok(expenseService.getCurrentMonthCategorySummary(userId));
    }

    /**
     * View a month-over-month spending trend.
     */
    @GetMapping("/trend/{userId}")
    public ResponseEntity<List<MonthlyTrendResponse>> getMonthOverMonthTrend(@PathVariable Long userId) {
        return ResponseEntity.ok(expenseService.getMonthOverMonthTrend(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "Expense deleted successfully"));
    }
}
