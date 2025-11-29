package com.lifeflow.bank.service;

import com.lifeflow.bank.model.BankTransaction;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class TatraStatementParser {

    @Value
    @Builder
    public static class Result {
        LocalDate periodFrom;
        LocalDate periodTo;
        List<BankTransaction> transactions;
    }

    // Если в выписке есть строка "Obdobie od 01.10.2025 do 31.10.2025"
    private static final Pattern PERIOD_PATTERN =
            Pattern.compile("Obdobie\\s+od\\s+(\\d{2}\\.\\d{2}\\.\\d{4})\\s+do\\s+(\\d{2}\\.\\d{2}\\.\\d{4})");

    /**
     * Голова операции — любая строка, начинающаяся с даты:
     *
     *  01.10.2025 AP nákup POS 10.96-
     *  08.10.2025 Platba 1100/000000-2932559444 48.00
     *  23.10.2025 Visa Direct 21.10.2025 610.00
     *  14.10.2025 Platba 0200/000000-4862337457 Prijatá platba: ...
     */
    private static final Pattern TX_HEAD_PATTERN =
            Pattern.compile("^(\\d{2}\\.\\d{2}\\.\\d{4})\\b.*$");

    /**
     * Вариант головы, где в КОНЦЕ строки уже есть сумма:
     *
     *  01.10.2025 AP nákup POS 10.96-
     *  25.10.2025 Platba 0200/000000-0003721858 670.00-
     *  27.10.2025 Vklad hotovosti cez bankomat 100.00
     */
    private static final Pattern TX_HEAD_WITH_AMOUNT =
            Pattern.compile("^(\\d{2}\\.\\d{2}\\.\\d{4}).*?(\\d+[.,]\\d{2})(-?)\\s*$");

    /**
     * Строка суммы внутри блока:
     *
     *  "Suma: 10.96- EUR"
     *  "Suma:48.00EUR"
     *  "Suma: 2,20EUR"
     */
    private static final Pattern SUMA_PATTERN =
            Pattern.compile("(?i)suma\\s*:?\\s*([0-9]+[.,][0-9]{2})(-?)");

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public Result parse(String text) {
        LocalDate from = null;
        LocalDate to = null;

        // 1) Период выписки (если есть)
        Matcher pm = PERIOD_PATTERN.matcher(text);
        if (pm.find()) {
            from = LocalDate.parse(pm.group(1), DATE_FMT);
            to = LocalDate.parse(pm.group(2), DATE_FMT);
        }

        List<BankTransaction> txs = new ArrayList<>();
        String[] lines = text.split("\\R+");

        // 2) Обход строк, поиск голов операций
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            Matcher headMatcher = TX_HEAD_PATTERN.matcher(line);
            if (!headMatcher.find()) {
                continue; // не начинается с даты — не наша операция
            }

            String dateStr = headMatcher.group(1);
            LocalDate localDate = LocalDate.parse(dateStr, DATE_FMT);
            OffsetDateTime bookedAt = localDate.atStartOfDay().atOffset(ZoneOffset.UTC);

            String description = line;

            BigDecimal amount = null;
            boolean isExpense = false;

            String merchant = null;   // Miesto platby: ...
            String receiver = null;   // Príjemca:
            String payer = null;      // Platiteľ:

            // --- 2.1. Пытаемся взять сумму и знак прямо из головы
            Matcher headAmountMatcher = TX_HEAD_WITH_AMOUNT.matcher(line);
            if (headAmountMatcher.find()) {
                String rawAmount = headAmountMatcher.group(2); // 10.96, 670.00
                String minusFlag = headAmountMatcher.group(3); // "-" или ""

                String normalized = rawAmount.replace(',', '.');
                BigDecimal value = new BigDecimal(normalized);

                isExpense = "-".equals(minusFlag);
                amount = isExpense ? value.negate() : value;
            }

            // --- 2.2. Собираем блок строк до следующей операции, чтобы:
            //     - подобрать Suma: ... если в голове не было суммы
            //     - понять контекст (Prijatá / Odoslaná / Visa Direct / Vklad / Výber)
            List<String> blockLines = new ArrayList<>();
            int j = i + 1;

            for (; j < lines.length; j++) {
                String next = lines[j].trim();
                if (next.isEmpty()) {
                    continue;
                }

                // если наткнулись на следующую операцию — выходим из блока
                if (TX_HEAD_PATTERN.matcher(next).find()) {
                    break;
                }

                blockLines.add(next);

                String lower = next.toLowerCase();

                if (lower.startsWith("miesto platby")) {
                    merchant = next;
                } else if (lower.startsWith("príjemca") || lower.startsWith("prijemca")) {
                    receiver = next;
                } else if (lower.startsWith("platiteľ") || lower.startsWith("platitel")) {
                    payer = next;
                }
            }

            // продвигаем i до конца блока
            i = j - 1;

            // --- 2.3. Если сумму из головы не получили — ищем Suma: ... в блоке
            if (amount == null) {
                BigDecimal sumaValue = null;
                String sumaMinus = null;

                for (String blk : blockLines) {
                    Matcher sm = SUMA_PATTERN.matcher(blk);
                    if (sm.find()) {
                        String raw = sm.group(1);      // "2.20", "48.00"
                        sumaMinus = sm.group(2);       // "-" или ""
                        String normalized = raw.replace(',', '.');
                        sumaValue = new BigDecimal(normalized);
                        break; // берём первую Suma в блоке
                    }
                }

                if (sumaValue == null) {
                    // не нашли сумму ни в голове, ни в Suma — пропускаем
                    log.warn("No amount found for tx head '{}', skipping", description);
                    continue;
                }

                // --- 2.4. Определяем знак по контексту блока
                String blockTextLower = String.join(" ", blockLines).toLowerCase();

                boolean hasOdoslana = blockTextLower.contains("odoslaná platba")
                        || blockTextLower.contains("odoslana platba");
                boolean hasPrijata = blockTextLower.contains("prijatá platba")
                        || blockTextLower.contains("prijata platba");
                boolean hasVisaDirect = blockTextLower.contains("visa direct");
                boolean hasVklad = blockTextLower.contains("vklad hotovosti");
                boolean hasVyber = blockTextLower.contains("výber z bankomatu")
                        || blockTextLower.contains("vyber z bankomatu");

                if ("-".equals(sumaMinus)) {
                    // если в Suma явно стоит "-", то это расход
                    isExpense = true;
                } else if (hasOdoslana || hasVyber) {
                    // Odoslaná platba / Výber z bankomatu — всегда расход
                    isExpense = true;
                } else if (hasPrijata || hasVisaDirect || hasVklad) {
                    // Prijatá platba / Visa Direct / Vklad hotovosti — всегда доход
                    isExpense = false;
                } else {
                    // запасной вариант: если нет "-", считаем доходом
                    isExpense = false;
                }

                amount = isExpense ? sumaValue.negate() : sumaValue;
            }

            // --- 2.5. Выбираем контрагента
            String counterparty = merchant;
            if (counterparty == null) {
                if (amount.signum() < 0 && receiver != null) {
                    counterparty = receiver;
                } else if (amount.signum() > 0 && payer != null) {
                    counterparty = payer;
                }
            }

            BankTransaction tx = BankTransaction.builder()
                    .bookedAt(bookedAt)
                    .amount(amount)
                    .currency("EUR")
                    .description(description)
                    .counterparty(counterparty)
                    .subscription(false)
                    .regular(false)
                    .build();

            txs.add(tx);
        }

        // 3) Для контроля — считаем DB/CR как в конце выписки
        BigDecimal db = txs.stream()
                .map(BankTransaction::getAmount)
                .filter(a -> a.signum() < 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .abs();

        BigDecimal cr = txs.stream()
                .map(BankTransaction::getAmount)
                .filter(a -> a.signum() > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info(
                "TatraStatementParser: parsed {} transactions, period {} - {}, DB={}, CR={}",
                txs.size(), from, to, db, cr
        );

        return Result.builder()
                .periodFrom(from)
                .periodTo(to)
                .transactions(txs)
                .build();
    }
}