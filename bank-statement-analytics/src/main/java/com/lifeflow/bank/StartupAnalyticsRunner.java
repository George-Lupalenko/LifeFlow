package com.lifeflow.bank;

import com.lifeflow.bank.dto.AnalyticsSummaryDto;
import com.lifeflow.bank.service.EmailStatementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartupAnalyticsRunner implements CommandLineRunner {

    private final EmailStatementService emailStatementService;

    @Value("${lifeflow.autorun.enabled:false}")
    private boolean autorunEnabled;

    @Override
    public void run(String... args) {
        if (!autorunEnabled) {
            log.info("StartupAnalyticsRunner: autorun disabled (lifeflow.autorun.enabled=false)");
            return;
        }

        try {
            log.info("StartupAnalyticsRunner: running email analytics for last {} statements", 6);
            long start = System.currentTimeMillis();

            List<AnalyticsSummaryDto> summaries = emailStatementService.fetchLastStatementsAndLogAnalytics();

            log.info("StartupAnalyticsRunner: got {} summaries from email", summaries.size());
            long end = System.currentTimeMillis();
            log.info("⏱ Endpoint /statements/last executed in {} ms ({} sec)",
                    (end - start), (end - start) / 1000.0);

        } catch (Exception e) {
            log.error("StartupAnalyticsRunner: failed to run email analytics", e);
        }
    }
}