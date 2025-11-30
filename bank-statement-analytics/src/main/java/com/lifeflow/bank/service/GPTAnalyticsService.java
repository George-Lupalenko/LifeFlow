package com.lifeflow.bank.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
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
Ты — финансовый аналитик. На вход тебе передаётся массив JSON-объектов. Каждый объект — это агрегированная статистика по тратам за один месяц.

Формат одного элемента массива:
{
  "totalExpenses": number,        // общая сумма расходов за месяц
  "totalIncome": number,          // общая сумма доходов за месяц
  "restaurantExpenses": number,   // расходы на рестораны
  "foodExpenses": number,         // расходы на продукты
  "subscriptionsExpenses": number,// расходы на подписки
  "categories": [
    {
      "code": string,             // код категории (например, FOOD_GROCERIES, OTHER)
      "name": string,             // человекочитаемое название категории
      "amount": number,           // сумма трат по категории
      "percentage": number        // доля категории в общих расходах (в процентах)
    },
    ...
  ]
}

Важно:
- Ты НЕ видишь отдельных транзакций, только агрегированные данные по месяцам.
- Тебе нужно проанализировать ДИНАМИКУ за несколько месяцев и дать человеку понятный, короткий, но содержательный обзор.

Формат ТВОЕГО ответа (строго один JSON-объект, без текста до или после):

{
  "monthsCount": number,                 // сколько месяцев в массиве
  "avgMonthlyExpenses": number,          // средние расходы в месяц (округли до 2 знаков)
  "avgMonthlyIncome": number,           // средние доходы в месяц (округли до 2 знаков)
  "savingsTrend": string,               // короткий вывод: "растёт", "падает" или "стабильно"
  "topSpendingCategories": [            // 3–5 ключевых категорий по средним расходам за период
    {
      "code": string,
      "name": string,
      "avgAmount": number,              // средний расход в месяц по категории
      "shareOfTotal": number            // средняя доля от всех расходов в %
    }
  ],
  "subscriptionsShare": {               // агрегированная аналитика по подпискам
    "avgSubscriptionsAmount": number,   // средние траты на подписки в месяц
    "avgSubscriptionsSharePercent": number, // средняя доля подписок в расходах, %
    "comment": string                   // короткий вывод: "подписки почти не чувствительны", "заметная статья расходов", и т.п.
  },
  "foodAndRestaurants": {               // еда и рестораны
    "avgFoodAmount": number,
    "avgRestaurantAmount": number,
    "comment": string
  },
  "keyInsights": [                      // 3–7 главных инсайтов на человеческом языке
    string
  ],
  "suggestedActions": [                 // 3–7 конкретных рекомендаций, что можно сделать
    string
  ]
}

Требования:
- ВСЕГДА возвращай строго валидный JSON, без комментариев, без лишнего текста.
- Числа округляй до двух знаков после запятой.
- Пиши по-русски, в дружелюбном тоне, но без воды.
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