package com.lifeflow.bank.service;

import com.lifeflow.bank.dto.AnalyticsSummaryDto;
import com.lifeflow.bank.dto.CategoryAnalyticsDto;
import com.lifeflow.bank.dto.SubscriptionDto;
import com.lifeflow.bank.model.BankTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final CategoryClassifier categoryClassifier;
    private final SubscriptionDetector subscriptionDetector;

    public AnalyticsSummaryDto analyze(List<BankTransaction> txs) {
        log.info("Starting analytics on {} raw transactions", txs.size());

        txs.stream()
                .limit(5)
                .forEach(tx -> log.info(
                        "TX: date={}, amount={}, currency={}, counterparty='{}', desc='{}'",
                        tx.getBookedAt(),
                        tx.getAmount(),
                        tx.getCurrency(),
                        tx.getCounterparty(),
                        tx.getDescription()
                ));

        // 1) категоризируем
        txs.forEach(tx -> {
            var cat = categoryClassifier.classify(tx);
            tx.setCategoryCode(cat.getCode());          // <= из enum CategoryResult
            tx.setCategoryName(cat.getDisplayName());   // <= красивое имя для фронта
        });

        // 2) ищем подписки
        subscriptionDetector.markSubscriptions(txs);

        // 3) агрегаты
        BigDecimal totalExp = sum(txs, a -> a.compareTo(BigDecimal.ZERO) < 0).abs();
        BigDecimal totalInc = sum(txs, a -> a.compareTo(BigDecimal.ZERO) > 0);

        Map<String, List<BankTransaction>> byCat = txs.stream()
                .filter(t -> t.getAmount().compareTo(BigDecimal.ZERO) < 0)
                .collect(Collectors.groupingBy(t ->
                        t.getCategoryCode() != null ? t.getCategoryCode() : "UNC"));

        List<CategoryAnalyticsDto> cats = new ArrayList<>();
        for (var e : byCat.entrySet()) {
            BigDecimal sum = e.getValue().stream()
                    .map(BankTransaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .abs();

            double pct = totalExp.signum() == 0
                    ? 0
                    : sum.multiply(BigDecimal.valueOf(100))
                    .divide(totalExp, 2, RoundingMode.HALF_UP)
                    .doubleValue();

            String name = Optional.ofNullable(e.getValue().get(0).getCategoryName())
                    .orElse("Без категории");

            cats.add(CategoryAnalyticsDto.builder()
                    .code(e.getKey())
                    .name(name)
                    .amount(sum)
                    .percentage(pct)
                    .build());
        }

        // Еда: продукты + доставка + кофе
        BigDecimal foodExp = sumByCodes(cats,
                "FOOD_GROCERIES",
                "FOOD_DELIVERY",
                "FOOD_COFFEE_SNACKS"
        );

        // Рестораны отдельно
        BigDecimal restExp = sumByCodes(cats,
                "FOOD_RESTAURANT"
        );

        // Подписки — по флагу isSubscription (его ставит SubscriptionDetector)
        BigDecimal subsExp = cats.stream()
                .filter(c -> c.code() != null && c.code().startsWith("SUBSCRIPTION_"))
                .map(CategoryAnalyticsDto::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<SubscriptionDto> subsTop = buildSubsTop(txs);

        String insight = "За период расходы " + totalExp +
                ", доходы " + totalInc +
                ". Больше всего трат в категории " +
                cats.stream()
                        .max(Comparator.comparing(CategoryAnalyticsDto::amount))
                        .map(CategoryAnalyticsDto::name)
                        .orElse("нет данных") + ".";

        log.info("Analytics result: totalExpenses={}, totalIncome={}, categories={}",
                totalExp, totalInc, cats.size());

        return AnalyticsSummaryDto.builder()
                .totalExpenses(totalExp)
                .totalIncome(totalInc)
                .restaurantExpenses(restExp)
                .foodExpenses(foodExp)
                .subscriptionsExpenses(subsExp)
                .categories(cats)
                .subscriptionsTop(subsTop)
                .insight(insight)
                .build();
    }

    private BigDecimal sum(List<BankTransaction> txs, Predicate<BigDecimal> filter) {
        return txs.stream()
                .map(BankTransaction::getAmount)
                .filter(Objects::nonNull)
                .filter(filter)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Новый helper: суммируем по нескольким кодам категорий
    private BigDecimal sumByCodes(List<CategoryAnalyticsDto> cats, String... codes) {
        if (codes == null || codes.length == 0) {
            return BigDecimal.ZERO;
        }
        Set<String> allowed = new HashSet<>(Arrays.asList(codes));
        return cats.stream()
                .filter(c -> allowed.contains(c.code()))
                .map(CategoryAnalyticsDto::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<SubscriptionDto> buildSubsTop(List<BankTransaction> txs) {
        return txs.stream()
                .filter(BankTransaction::isSubscription)
                .collect(Collectors.groupingBy(
                        t -> Optional.ofNullable(t.getCounterparty()).orElse("Unknown")
                ))
                .entrySet().stream()
                .map(e -> {
                    BigDecimal sum = e.getValue().stream()
                            .map(BankTransaction::getAmount)
                            .map(BigDecimal::abs)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal avg = sum.divide(
                            BigDecimal.valueOf(e.getValue().size()),
                            2, RoundingMode.HALF_UP
                    );
                    return SubscriptionDto.builder()
                            .merchant(e.getKey())
                            .avgAmount(avg)
                            .occurrences(e.getValue().size())
                            .build();
                })
                .sorted(Comparator.comparing(SubscriptionDto::avgAmount).reversed())
                .limit(10)
                .toList();
    }
}