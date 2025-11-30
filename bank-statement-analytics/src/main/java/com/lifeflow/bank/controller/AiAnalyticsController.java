package com.lifeflow.bank.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lifeflow.bank.dto.AnalyticsSummaryDto;
import com.lifeflow.bank.dto.UserEmailRequest;
import com.lifeflow.bank.service.EmailStatementService;
import com.lifeflow.bank.service.GPTAnalyticsService;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/analytics")
public class AiAnalyticsController {

    private final EmailStatementService emailStatementService;
    private final GPTAnalyticsService gptAnalyticsService;
    private final ObjectMapper objectMapper;

    /**
     * POST /analytics/ai/user
     *
     * Body:
     * {
     *   "username": "user@gmail.com",
     *   "password": "gmail_app_password",
     *   "pdfPassword": "pdf_password_for_statements",
     *   "lastCount": 6
     * }
     */
    @PostMapping("/ai/user")
    public ResponseEntity<JsonNode> getUserAiAnalytics(@RequestBody UserEmailRequest req) {

        // --- ВАЛИДАЦИЯ ПАРАМЕТРОВ (400) ---
        if (req.username() == null || req.username().isBlank()) {
            return badRequest("username (email) is required");
        }
        if (req.password() == null || req.password().isBlank()) {
            return badRequest("password (Gmail app password) is required");
        }
        if (req.pdfPassword() == null || req.pdfPassword().isBlank()) {
            return badRequest("pdfPassword (password for TatraBank PDF) is required");
        }
        if (req.lastCount() <= 0) {
            return badRequest("lastCount must be > 0");
        }

        List<AnalyticsSummaryDto> summaries;

        // --- ПОЛУЧЕНИЕ ВЫПИСОК ---
        try {
            summaries = emailStatementService.fetchLastStatementsAndLogAnalytics(
                    req.username(),
                    req.password(),
                    req.lastCount(),
                    req.pdfPassword()
            );
        } catch (Exception e) {
            log.error("Unexpected email processing error", e);
            return serverError("Email parsing error: " + e.getMessage());
        }

        if (summaries.isEmpty()) {
            return notFound("No TatraBanka statements found in mailbox");
        }

        int monthsCount = summaries.size();

        // --- GPT АНАЛИЗ ---
        String aiResult;
        try {
            aiResult = gptAnalyticsService.generateAnalytics(summaries);
        } catch (Exception e) {
            log.error("GPT error", e);
            return serverError("AI analytics failed: " + e.getMessage());
        }

        // --- ФОРМИРОВАНИЕ JSON (200 OK) ---
        try {
            JsonNode json = objectMapper.readTree(aiResult);
            if (json.isObject()) {
                ((ObjectNode) json).put("monthsCount", monthsCount);
            }
            return ResponseEntity.ok(json);
        } catch (Exception e) {
            ObjectNode raw = objectMapper.createObjectNode();
            raw.put("raw", aiResult);
            raw.put("monthsCount", monthsCount);
            return ResponseEntity.ok(raw);
        }
    }

    // =====================================================================
    //              ХЕЛПЕРЫ ДЛЯ ВОЗВРАТА ОШИБОК С КОДАМИ
    // =====================================================================

    private ResponseEntity<JsonNode> badRequest(String msg) {
        return ResponseEntity.status(400).body(error(msg));
    }

    private ResponseEntity<JsonNode> unauthorized(String msg) {
        return ResponseEntity.status(401).body(error(msg));
    }

    private ResponseEntity<JsonNode> notFound(String msg) {
        return ResponseEntity.status(404).body(error(msg));
    }

    private ResponseEntity<JsonNode> serverError(String msg) {
        return ResponseEntity.status(500).body(error(msg));
    }

    private ObjectNode error(String message) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("status", "error");
        node.put("message", message);
        return node;
    }
}