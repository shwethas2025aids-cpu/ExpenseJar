package com.example.expensejar.dto;

public class MonthlyTrendResponse {

    private String month;
    private Double totalSpent;

    public MonthlyTrendResponse() {
    }

    public MonthlyTrendResponse(String month, Double totalSpent) {
        this.month = month;
        this.totalSpent = totalSpent != null ? Math.round(totalSpent * 100.0) / 100.0 : 0.0;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(Double totalSpent) {
        this.totalSpent = totalSpent != null ? Math.round(totalSpent * 100.0) / 100.0 : 0.0;
    }
}
