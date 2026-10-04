package com.financeflow.dto;

import java.math.BigDecimal;
import java.util.List;

public class MonthlySummaryResponse {

    private int year;
    private int month;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netSavings;
    private List<CategorySummaryResponse> categoryBreakdown;

    public MonthlySummaryResponse(int year, int month, BigDecimal totalIncome, BigDecimal totalExpense,
                                  BigDecimal netSavings, List<CategorySummaryResponse> categoryBreakdown) {
        this.year = year;
        this.month = month;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netSavings = netSavings;
        this.categoryBreakdown = categoryBreakdown;
    }

    public int getYear() { return year; }
    public int getMonth() { return month; }
    public BigDecimal getTotalIncome() { return totalIncome; }
    public BigDecimal getTotalExpense() { return totalExpense; }
    public BigDecimal getNetSavings() { return netSavings; }
    public List<CategorySummaryResponse> getCategoryBreakdown() { return categoryBreakdown; }
}
