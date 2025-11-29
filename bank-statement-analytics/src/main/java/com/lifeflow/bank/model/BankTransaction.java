package com.lifeflow.bank.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
public class BankTransaction {

    private OffsetDateTime bookedAt;
    private BigDecimal amount;      // минус = расход
    private String currency;

    private String description;
    private String counterparty;

    private String categoryCode;    // FOOD / RESTAURANT / ...
    private String categoryName;
    private boolean subscription;
    private boolean regular;

    private CategoryResult category;
}