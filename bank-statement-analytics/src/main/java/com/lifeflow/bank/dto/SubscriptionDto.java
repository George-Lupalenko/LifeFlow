package com.lifeflow.bank.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record SubscriptionDto(
        String merchant,
        BigDecimal avgAmount,
        int occurrences
) {
}