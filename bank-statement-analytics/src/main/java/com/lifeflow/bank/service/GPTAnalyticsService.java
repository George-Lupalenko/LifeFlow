package com.lifeflow.bank.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Slf4j
public class GPTAnalyticsService {

    @Value("${LIFEFLOW_OPENAI_API_KEY}")
    private String apiKey;

    private final ObjectMapper objectMapper;

    private static final String GPT_URL = "https://api.openai.com/v1/chat/completions";

    private final OkHttpClient client = new OkHttpClient();

    private static final String PROMPT = """
You are a financial analyst. You receive an array of JSON objects. Each object represents aggregated spending statistics for one month.

Format of a single element in the array:
{
  "totalExpenses": number,        
  "totalIncome": number,          
  "restaurantExpenses": number,   
  "foodExpenses": number,         
  "subscriptionsExpenses": number,
  "categories": [
    {
      "code": string,
      "name": string,
      "amount": number,
      "percentage": number
    }
  ]
}

Important:
- You DO NOT see individual transactions, only aggregated monthly summaries.
- You must analyze MULTI-MONTH TRENDS and produce a clear, short but insightful overview.

Your response must be EXACTLY ONE JSON OBJECT. No explanations, no text before or after.

Return strictly the following format:

{
  "monthsCount": number,
  "avgMonthlyExpenses": number,
  "avgMonthlyIncome": number,
  "savingsTrend": string,                
  "topSpendingCategories": [
    {
      "code": string,
      "name": string,
      "avgAmount": number,
      "shareOfTotal": number
    }
  ],
  "subscriptionsShare": {
    "avgSubscriptionsAmount": number,
    "avgSubscriptionsSharePercent": number,
    "comment": string
  },
  "foodAndRestaurants": {
    "avgFoodAmount": number,
    "avgRestaurantAmount": number,
    "comment": string
  },
  "keyInsights": [
    string
  ],
  "suggestedActions": [
    string
  ]
}

Rules:
- ALWAYS respond in English.
- ALWAYS return strictly valid JSON.
- No comments, no surrounding text.
- Round all numbers to 2 decimal places.
- Tone: friendly, concise, professional.
""";

    public String generateAnalytics(Object summariesJson) {
        try {
            // summariesJson — это уже List<AnalyticsSummaryDto> из контроллера
            String jsonInput = objectMapper.writeValueAsString(summariesJson);
            log.info("GPTAnalyticsService: sending {} chars to GPT", jsonInput.length());

            // Собираем body как объект, а не руками строкой
            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", PROMPT);

            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", jsonInput);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4.1-mini");
            requestBody.put("messages", List.of(systemMessage, userMessage));
            requestBody.put("temperature", 0);

            String bodyJson = objectMapper.writeValueAsString(requestBody);

            MediaType JSON = MediaType.parse("application/json");
            RequestBody requestBodyOk = RequestBody.create(bodyJson, JSON);

            Request request = new Request.Builder()
                    .url(GPT_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .post(requestBodyOk)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    throw new IllegalStateException(
                            "GPT API error: " + response.code() + " body=" + errorBody
                    );
                }

                String responseJson = response.body().string();

                String result = objectMapper.readTree(responseJson)
                        .get("choices").get(0)
                        .get("message").get("content")
                        .asText();

                log.info("GPTAnalyticsService: got response {} chars", result.length());
                return result;
            }

        } catch (Exception e) {
            log.error("GPTAnalyticsService: error communicating with GPT", e);
            throw new RuntimeException("GPT analytics failed", e);
        }
    }
}