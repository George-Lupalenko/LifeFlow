package com.lifeflow.bank.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record AnalyticsSummaryDto(
        BigDecimal totalExpenses,
        BigDecimal totalIncome,
        BigDecimal restaurantExpenses,
        BigDecimal foodExpenses,
        BigDecimal subscriptionsExpenses,
        List<CategoryAnalyticsDto> categories,
        List<SubscriptionDto> subscriptionsTop,
        String insight
) {
}