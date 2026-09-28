package com.example.expensejar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class ExpenseResponse {

    private Long id;
    private Double amount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private Long userId;
    private String userName;
    private Long categoryId;
    private String categoryName;

    public ExpenseResponse() {
    }

    public ExpenseResponse(Long id, Double amount, LocalDate date, Long userId, String userName, Long categoryId, String categoryName) {
        this.id = id;
        this.amount = amount;
        this.date = date;
        this.userId = userId;
        this.userName = userName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
