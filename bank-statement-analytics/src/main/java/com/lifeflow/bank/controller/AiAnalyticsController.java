package com.lifeflow.bank.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lifeflow.bank.dto.AnalyticsSummaryDto;
import com.lifeflow.bank.dto.EmailCredentialsRequest;
import com.lifeflow.bank.dto.UserEmailRequest;
import com.lifeflow.bank.service.EmailStatementService;
import com.lifeflow.bank.service.GPTAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/analytics")
public class AiAnalyticsController {

    private final EmailStatementService emailStatementService;
    private final GPTAnalyticsService gptAnalyticsService;
    private final ObjectMapper objectMapper;

    @GetMapping("/ai")
    public JsonNode getAiAnalytics() {
        long start = System.currentTimeMillis();

        List<AnalyticsSummaryDto> summaries = emailStatementService.fetchLastStatementsAndLogAnalytics();

        String result = gptAnalyticsService.generateAnalytics(summaries);

        long ms = System.currentTimeMillis() - start;
        log.info("AI analytics complete in {} ms", ms);

        try {
            JsonNode root = objectMapper.readTree(result);

            if (root.isObject()) {
                ((com.fasterxml.jackson.databind.node.ObjectNode) root)
                        .put("monthsCount", summaries.size()); // <- ЖЁСТКО ЗАТИРАЕМ
            }

            return root;
        } catch (Exception e) {
            log.error("Failed to parse GPT JSON, returning raw text", e);
            return objectMapper.createObjectNode().put("raw", result);
        }
    }

    @GetMapping("/ai/full")
    public Map<String, Object> getFullAiAnalytics() {
        long start = System.currentTimeMillis();

        List<AnalyticsSummaryDto> summaries = emailStatementService.fetchLastStatementsAndLogAnalytics();
        String aiJson = gptAnalyticsService.generateAnalytics(summaries);

        long ms = System.currentTimeMillis() - start;
        log.info("AI analytics complete in {} ms", ms);

        Map<String, Object> response = new HashMap<>();
        response.put("rawSummaries", summaries);

        try {
            response.put("aiReport", objectMapper.readTree(aiJson));
        } catch (Exception e) {
            response.put("aiReportRaw", aiJson);
        }

        return response;
    }

    /**
     * POST /analytics/ai/by-email
     *
     * Body:
     * {
     *   "email": "user@gmail.com",
     *   "appPassword": "abcd efgh ijkl mnop",
     *   "lastCount": 6
     * }
     */
    @PostMapping("/ai/by-email")
    public JsonNode getAiAnalyticsByEmail(@RequestBody EmailCredentialsRequest request) {
        long start = System.currentTimeMillis();

        String imapHost = "imap.gmail.com"; // для Gmail можно захардкодить
        String username = request.getEmail();
        String password = request.getAppPassword();
        int lastCount = request.getLastCount() != null ? request.getLastCount() : 6;

        // 1) Считываем выписки с почты конкретного юзера
        List<AnalyticsSummaryDto> summaries =
                emailStatementService.fetchLastStatementsAndLogAnalytics(
                        imapHost,
                        username,
                        password,
                        lastCount
                );

        // 2) Гоняем через GPT
        String result = gptAnalyticsService.generateAnalytics(summaries);

        long ms = System.currentTimeMillis() - start;
        log.info("AI analytics for {} complete in {} ms, months={}", username, ms, summaries.size());

        try {
            return objectMapper.readTree(result);
        } catch (Exception e) {
            log.error("Failed to parse GPT JSON, returning raw text", e);
            return objectMapper.createObjectNode().put("raw", result);
        }
    }

    @PostMapping("/ai/user")
    public JsonNode getUserAiAnalytics(@RequestBody UserEmailRequest req) {
        long start = System.currentTimeMillis();

        // 1. Тянем выписки из почты пользователя
        List<AnalyticsSummaryDto> summaries =
                emailStatementService.fetchLastStatementsAndLogAnalytics(
                        req.imapHost(),
                        req.username(),
                        req.password(),
                        req.lastCount()
                );

        int monthsCount = summaries.size();
        log.warn("Real months count from backend = {}", monthsCount);

        // 2. GPT-репорт (строкой)
        String result = gptAnalyticsService.generateAnalytics(summaries);

        long ms = System.currentTimeMillis() - start;
        log.info("AI analytics complete in {} ms", ms);

        try {
            // 3. Парсим строку в JSON
            JsonNode node = objectMapper.readTree(result);

            // 4. Насильно перезаписываем monthsCount
            if (node.isObject()) {
                ((ObjectNode) node).put("monthsCount", monthsCount);
            }

            return node;
        } catch (Exception e) {
            log.error("Failed to parse GPT JSON, returning raw text", e);
            // fallback — оборачиваем сырую строку
            ObjectNode wrapper = objectMapper.createObjectNode();
            wrapper.put("raw", result);
            wrapper.put("monthsCount", monthsCount); // хотя бы тут видеть правильное число
            return wrapper;
        }
    }
}