package com.lifeflow.bank;

import com.lifeflow.bank.dto.AnalyticsSummaryDto;
import com.lifeflow.bank.model.BankTransaction;
import com.lifeflow.bank.service.AnalyticsService;
import com.lifeflow.bank.service.LocalStatementFileService;
import com.lifeflow.bank.service.PdfStatementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartupAnalyticsRunner implements CommandLineRunner {

    private final PdfStatementService pdfStatementService;
    private final AnalyticsService analyticsService;
    private final LocalStatementFileService localStatementFileService;

    // Включить/выключить автозапуск
    @Value("${lifeflow.autorun.enabled:false}")
    private boolean autorunEnabled;

    // Часть имени файла для автозапуска
    @Value("${lifeflow.autorun.namePart:}")
    private String autorunNamePart;

    @Override
    public void run(String... args) {
        if (!autorunEnabled) {
            log.info("StartupAnalyticsRunner: autorun disabled (lifeflow.autorun.enabled=false)");
            return;
        }
        if (autorunNamePart == null || autorunNamePart.isBlank()) {
            log.warn("StartupAnalyticsRunner: autorun enabled but lifeflow.autorun.namePart is empty");
            return;
        }

        try {
            log.info("StartupAnalyticsRunner: running analytics for '{}'", autorunNamePart);
            File pdf = localStatementFileService.findStatementByNamePart(autorunNamePart);
            try (InputStream is = new FileInputStream(pdf)) {
                List<BankTransaction> txs = pdfStatementService.parseTatraStatementPdf(is);
                AnalyticsSummaryDto summary = analyticsService.analyze(txs);
                log.info("StartupAnalyticsRunner result for '{}': {}", autorunNamePart, summary);
            }
        } catch (Exception e) {
            log.error("StartupAnalyticsRunner: failed to run analytics for '{}'", autorunNamePart, e);
        }
    }
}