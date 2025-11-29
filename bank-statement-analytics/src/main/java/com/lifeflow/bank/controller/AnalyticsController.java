package com.lifeflow.bank.controller;

import com.lifeflow.bank.dto.AnalyticsSummaryDto;
import com.lifeflow.bank.model.BankTransaction;
import com.lifeflow.bank.service.AnalyticsService;
import com.lifeflow.bank.service.EmailStatementService;
import com.lifeflow.bank.service.LocalStatementFileService;
import com.lifeflow.bank.service.PdfStatementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final PdfStatementService pdfStatementService;
    private final AnalyticsService analyticsService;
    private final LocalStatementFileService localStatementFileService;
    private final EmailStatementService emailStatementService;

    @GetMapping("/from-local")
    public AnalyticsSummaryDto analyzeFromLocal(@RequestParam("namePart") String namePart) throws Exception {
        log.info("Received /api/analytics/from-local, namePart={}", namePart);

        File pdf = localStatementFileService.findStatementByNamePart(namePart);
        try (InputStream is = new FileInputStream(pdf)) {
            List<BankTransaction> txs = pdfStatementService.parseTatraStatementPdf(is);
            log.info("Parsed {} transactions from local PDF (namePart={})", txs.size(), namePart);
            return analyticsService.analyze(txs);
        }
    }

    /**
     * GET http://localhost:8081/api/analytics/from-email
     *
     * Вернёт список AnalyticsSummaryDto — по одному на каждую найденную выписку из email.
     */
    @GetMapping("/from-email")
    public List<AnalyticsSummaryDto> analyzeFromEmail() {
        long start = System.currentTimeMillis();

        List<AnalyticsSummaryDto> result =
                emailStatementService.fetchLastStatementsAndLogAnalytics();

        long end = System.currentTimeMillis();
        log.info("⏱ Endpoint /statements/last executed in {} ms ({} sec)",
                (end - start), (end - start) / 1000.0);

        return result;
    }
}