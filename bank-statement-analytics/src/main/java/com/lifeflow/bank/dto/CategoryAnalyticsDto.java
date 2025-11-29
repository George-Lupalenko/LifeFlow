package com.lifeflow.bank.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CategoryAnalyticsDto(
        String code,
        String name,
        BigDecimal amount,
        double percentage
) {
}