package com.example.expensejar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public class BudgetRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Budget amount is required")
    @Positive(message = "Budget amount must be greater than zero")
    private Double amount;

    @NotBlank(message = "Month is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "Month must be in YYYY-MM format (e.g., 2026-09)")
    private String month;

    public BudgetRequest() {
    }

    public BudgetRequest(Long userId, Long categoryId, Double amount, String month) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.month = month;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }
}
