package com.financeflow.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CategoryResponse {

    private Long id;
    private String name;
    private String color;
    private BigDecimal monthlyBudget;
    private LocalDateTime createdAt;

    public CategoryResponse(Long id, String name, String color, BigDecimal monthlyBudget, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.monthlyBudget = monthlyBudget;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getColor() { return color; }
    public BigDecimal getMonthlyBudget() { return monthlyBudget; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
