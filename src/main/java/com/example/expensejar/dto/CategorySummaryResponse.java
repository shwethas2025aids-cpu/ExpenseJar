package com.example.expensejar.dto;

public class CategorySummaryResponse {

    private String category;
    private Double totalSpent;

    public CategorySummaryResponse() {
    }

    public CategorySummaryResponse(String category, Double totalSpent) {
        this.category = category;
        this.totalSpent = totalSpent != null ? Math.round(totalSpent * 100.0) / 100.0 : 0.0;
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
        this.totalSpent = totalSpent != null ? Math.round(totalSpent * 100.0) / 100.0 : 0.0;
    }
}
