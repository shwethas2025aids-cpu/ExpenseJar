package com.example.expensejar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class ExpenseRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Expense amount is required")
    @Positive(message = "Expense amount must be greater than zero")
    private Double amount;

    @NotNull(message = "Date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    public ExpenseRequest() {
    }

    public ExpenseRequest(Long userId, Long categoryId, Double amount, LocalDate date) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.date = date;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
