package com.financeflow.service;

import com.financeflow.dto.CategorySummaryResponse;
import com.financeflow.dto.MonthlySummaryResponse;
import com.financeflow.entity.TransactionType;
import com.financeflow.repository.TransactionRepository;
import com.financeflow.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnalyticsService {

    private final TransactionRepository transactionRepository;

    public AnalyticsService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public MonthlySummaryResponse getMonthlySummary(int year, int month, UserPrincipal principal) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        BigDecimal income = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                principal.getId(), TransactionType.INCOME, start, end);
        if (income == null) income = BigDecimal.ZERO;

        BigDecimal expense = transactionRepository.sumAmountByUserIdAndTypeAndDateRange(
                principal.getId(), TransactionType.EXPENSE, start, end);
        if (expense == null) expense = BigDecimal.ZERO;

        BigDecimal savings = income.subtract(expense);

        List<Object[]> rawCategoryData = transactionRepository.findExpenseSummaryByCategoryAndDateRange(
                principal.getId(), start, end);

        List<CategorySummaryResponse> breakdowns = new ArrayList<>();
        for (Object[] row : rawCategoryData) {
            Long catId = (Long) row[0];
            String catName = (String) row[1];
            String catColor = (String) row[2];
            BigDecimal total = (BigDecimal) row[3];

            double percentage = 0.0;
            if (expense.compareTo(BigDecimal.ZERO) > 0) {
                percentage = total.divide(expense, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
            }

            breakdowns.add(new CategorySummaryResponse(catId, catName, catColor, total, percentage));
        }

        return new MonthlySummaryResponse(year, month, income, expense, savings, breakdowns);
    }
}
