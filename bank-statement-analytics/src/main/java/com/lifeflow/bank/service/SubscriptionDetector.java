package com.lifeflow.bank.service;

import com.lifeflow.bank.model.BankTransaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubscriptionDetector {

    /**
     * Помечает транзакции как подписки/регулярные по эвристике:
     * - одинаковый мерчант + примерно одинаковая сумма
     * - минимум 3 операции
     * - интервалы между операциями ~ раз в месяц (20–40 дней)
     */
    public void markSubscriptions(List<BankTransaction> txs) {
        Map<String, List<BankTransaction>> groups = txs.stream()
                .filter(tx -> tx.getAmount().compareTo(BigDecimal.ZERO) < 0)
                .collect(Collectors.groupingBy(this::key));

        for (List<BankTransaction> g : groups.values()) {
            if (g.size() < 3) continue;

            g.sort(Comparator.comparing(BankTransaction::getBookedAt));
            boolean monthly = true;

            for (int i = 1; i < g.size(); i++) {
                long days = Duration.between(
                        g.get(i - 1).getBookedAt(),
                        g.get(i).getBookedAt()
                ).toDays();
                if (days < 20 || days > 40) {
                    monthly = false;
                    break;
                }
            }

            if (monthly) {
                g.forEach(tx -> {
                    tx.setSubscription(true);
                    tx.setRegular(true);
                });
            }
        }
    }

    private String key(BankTransaction tx) {
        BigDecimal rounded = tx.getAmount().abs().setScale(0, RoundingMode.HALF_UP);
        String cp = Optional.ofNullable(tx.getCounterparty())
                .orElse("")
                .toUpperCase(Locale.ROOT);
        return cp + "|" + rounded;
    }
}