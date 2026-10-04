package com.financeflow.dto;

import java.math.BigDecimal;

public class CategorySummaryResponse {

    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private BigDecimal totalAmount;
    private double percentage;

    public CategorySummaryResponse(Long categoryId, String categoryName, String categoryColor, BigDecimal totalAmount, double percentage) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryColor = categoryColor;
        this.totalAmount = totalAmount;
        this.percentage = percentage;
    }

    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getCategoryColor() { return categoryColor; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public double getPercentage() { return percentage; }
}
