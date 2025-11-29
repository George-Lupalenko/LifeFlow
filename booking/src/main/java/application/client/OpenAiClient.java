package application.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI Client for AI operations
 * Uses OpenAI API instead of Gemini
 */
@Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        name = "mock.openai.enabled",
        havingValue = "true",
        matchIfMissing = true
)@Slf4j
public class OpenAiClient implements AiClient {
    
    private final WebClient webClient;
    private final String apiKey;
    private final String model;
    private static final String OPENAI_API_BASE_URL = "https://api.openai.com/v1";
    
    public OpenAiClient(@Value("${openai.api.key}") String apiKey,
                       @Value("${openai.model:gpt-4o-mini}") String model) {
        // Create OpenAI WebClient with authentication
        this.webClient = WebClient.builder()
                .baseUrl(OPENAI_API_BASE_URL)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.apiKey = apiKey;
        this.model = model;
        log.info("OpenAI Client initialized with model: {}", model);
    }
    
    /**
     * Extract travel intent from natural language query using OpenAI
     */
    @Override
    public Mono<String> extractTravelIntent(String userQueryOrPrompt) {
        // If it's a short query, build a prompt; if it's already a prompt, use it as-is
        String prompt = userQueryOrPrompt.length() < 200 && !userQueryOrPrompt.contains("Extract travel information") ?
            String.format(
                "Extract travel information from this query and return ONLY valid JSON with these fields: " +
                "originLocation (city or IATA code), destinationLocation (city or IATA code), " +
                "departureDate (YYYY-MM-DD), returnDate (YYYY-MM-DD or null), " +
                "numberOfAdults (integer), numberOfChildren (integer), " +
                "maxBudget (number or null), preferredClass (ECONOMY/BUSINESS/FIRST or null), " +
                "preferences (string with any special requirements). " +
                "User query: %s", userQueryOrPrompt
            ) : userQueryOrPrompt;
        
        return callOpenAI(prompt);
    }
    
    /**
     * Call OpenAI API with a custom prompt
     */
    private Mono<String> callOpenAI(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
            Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.3);
        requestBody.put("response_format", Map.of("type", "json_object"));
        
        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {
                    // Extract text from OpenAI response
                    try {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                        if (choices == null || choices.isEmpty()) {
                            log.warn("No choices in OpenAI response");
                            return "{}";
                        }
                        Map<String, Object> choice = choices.get(0);
                        Map<String, Object> message = (Map<String, Object>) choice.get("message");
                        String content = (String) message.get("content");
                        return content != null ? content : "{}";
                    } catch (Exception e) {
                        log.error("Error parsing OpenAI response", e);
                        return "{}";
                    }
                })
                .doOnError(error -> log.error("Error calling OpenAI API", error))
                .onErrorReturn("{}");
    }
    
    /**
     * Generate personalized travel recommendations
     */
    @Override
    public Mono<String> generateRecommendations(String travelContext, List<String> flightOptions, List<String> hotelOptions) {
        String prompt = String.format(
            "Based on this travel context: %s\n\n" +
            "Flight options: %s\n\n" +
            "Hotel options: %s\n\n" +
            "Provide a personalized recommendation explaining which options are best and why. " +
            "Consider price, convenience, quality, and user preferences. " +
            "Be concise and helpful.",
            travelContext, flightOptions, hotelOptions
        );
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
            Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.7);
        
        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {
                    try {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                        if (choices == null || choices.isEmpty()) {
                            return "Unable to generate recommendations at this time.";
                        }
                        Map<String, Object> choice = choices.get(0);
                        Map<String, Object> message = (Map<String, Object>) choice.get("message");
                        String content = (String) message.get("content");
                        return content != null ? content : "Unable to generate recommendations at this time.";
                    } catch (Exception e) {
                        log.error("Error parsing OpenAI response", e);
                        return "Unable to generate recommendations at this time.";
                    }
                })
                .doOnError(error -> log.error("Error calling OpenAI API for recommendations", error))
                .onErrorReturn("Unable to generate recommendations at this time.");
    }
}
