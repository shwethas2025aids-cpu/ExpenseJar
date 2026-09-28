package com.example.expensejar.controller;

import com.example.expensejar.dto.BudgetRequest;
import com.example.expensejar.entity.Budget;
import com.example.expensejar.service.BudgetService;
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
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<Budget> setBudget(@Valid @RequestBody BudgetRequest request) {
        Budget createdBudget = budgetService.setBudget(request);
        return new ResponseEntity<>(createdBudget, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Budget>> getAllBudgets() {
        return ResponseEntity.ok(budgetService.getAllBudgets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Budget> getBudgetById(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getBudgetById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Budget>> getBudgetsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(budgetService.getBudgetsByUserId(userId));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Budget>> getBudgetsByCategoryId(@PathVariable Long categoryId) {
        return ResponseEntity.ok(budgetService.getBudgetsByCategoryId(categoryId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteBudget(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.ok(Collections.singletonMap("message", "Budget deleted successfully"));
    }
}
