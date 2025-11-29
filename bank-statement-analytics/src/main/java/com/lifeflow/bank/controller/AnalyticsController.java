package com.lifeflow.bank.controller;

import com.lifeflow.bank.dto.AnalyticsSummaryDto;
import com.lifeflow.bank.model.BankTransaction;
import com.lifeflow.bank.service.AnalyticsService;
import com.lifeflow.bank.service.LocalStatementFileService;
import com.lifeflow.bank.service.PdfStatementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * Пример запроса:
     * GET http://localhost:8081/api/analytics/from-local?namePart=89966_00_12514_2025-11-01
     */
    @GetMapping("/from-local")
    public AnalyticsSummaryDto analyzeFromLocal(
            @RequestParam("namePart") String namePart
    ) throws Exception {

        log.info("Received /api/analytics/from-local, namePart={}", namePart);

        File pdf = localStatementFileService.findStatementByNamePart(namePart);

        try (InputStream is = new FileInputStream(pdf)) {
            List<BankTransaction> txs = pdfStatementService.parseTatraStatementPdf(is);
            log.info("Parsed {} transactions from local PDF (namePart={})", txs.size(), namePart);
            return analyticsService.analyze(txs);
        }
    }
}