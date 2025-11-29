package application.client;

import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Interface for AI clients (Gemini, etc.)
 */
public interface AiClient {
    Mono<String> extractTravelIntent(String userQueryOrPrompt);
    Mono<String> generateRecommendations(String travelContext, List<String> flightOptions, List<String> hotelOptions);
}

