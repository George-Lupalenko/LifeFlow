package com.lifeflow.bank;

import com.lifeflow.bank.dto.AnalyticsSummaryDto;
import com.lifeflow.bank.service.EmailStatementService;
import com.lifeflow.bank.service.GPTAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartupAiAnalyticsRunner implements CommandLineRunner {

    private final EmailStatementService emailStatementService;
    private final GPTAnalyticsService gptAnalyticsService;

    @Value("${lifeflow.autorun.ai-enabled:false}")
    private boolean aiEnabled;

    @Override
    public void run(String... args) {
        if (!aiEnabled) {
            log.info("StartupAiAnalyticsRunner: AI autorun disabled (lifeflow.autorun.ai-enabled=false)");
            return;
        }

        try {
            long start = System.currentTimeMillis();

            log.info("StartupAiAnalyticsRunner: starting AI analytics...");

            // 1. Получаем данные как при ручном вызове
            List<AnalyticsSummaryDto> summaries =
                    emailStatementService.fetchLastStatementsAndLogAnalytics();

            log.info("StartupAiAnalyticsRunner: got {} statement summaries", summaries.size());

            if (summaries.isEmpty()) {
                log.warn("StartupAiAnalyticsRunner: no summaries found, skipping AI analytics");
                return;
            }

            // 2. Отправляем в GPT
            String aiReport = gptAnalyticsService.generateAnalytics(summaries);

            long ms = System.currentTimeMillis() - start;

            log.info("StartupAiAnalyticsRunner: AI analytics completed in {} ms", ms);
            log.info("===== AI ANALYTICS REPORT BEGIN =====");
            log.info("\n" + aiReport);
            log.info("===== AI ANALYTICS REPORT END =====");

        } catch (Exception e) {
            log.error("StartupAiAnalyticsRunner: failed to run AI analytics", e);
        }
    }
}