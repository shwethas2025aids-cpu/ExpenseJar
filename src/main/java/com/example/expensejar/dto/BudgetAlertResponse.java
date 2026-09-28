package com.example.expensejar.dto;

public class BudgetAlertResponse {

    private String message;
    private String category;
    private Double totalSpent;
    private Double budget;
    private Double percentageUsed;
    private boolean alert;

    public BudgetAlertResponse() {
    }

    public BudgetAlertResponse(String message, String category, Double totalSpent, Double budget, Double percentageUsed, boolean alert) {
        this.message = message;
        this.category = category;
        this.totalSpent = totalSpent;
        this.budget = budget;
        this.percentageUsed = percentageUsed;
        this.alert = alert;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(Double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public Double getPercentageUsed() {
        return percentageUsed;
    }

    public void setPercentageUsed(Double percentageUsed) {
        this.percentageUsed = percentageUsed;
    }

    public boolean isAlert() {
        return alert;
    }

    public void setAlert(boolean alert) {
        this.alert = alert;
    }
}
