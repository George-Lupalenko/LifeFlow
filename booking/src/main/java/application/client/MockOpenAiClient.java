package application.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock OpenAI Client for testing without API keys
 * Enable with: mock.gemini.enabled=true in application.properties
 */
@Service
@ConditionalOnProperty(name = "mock.gemini.enabled", havingValue = "true", matchIfMissing = false)
@org.springframework.context.annotation.Primary
@Slf4j
public class MockOpenAiClient implements AiClient {
    
    public MockOpenAiClient() {
        log.info("Using MOCK OpenAI Client - no real API calls will be made");
    }
    
    public Mono<String> extractTravelIntent(String userQueryOrPrompt) {
        log.info("MOCK: Extracting travel intent from: {}", userQueryOrPrompt);
        
        // Simple mock extraction - parse common patterns
        String query = userQueryOrPrompt.toLowerCase();
        
        // Extract destination
        String destination = "Paris";
        if (query.contains("tokyo") || query.contains("japan")) destination = "Tokyo";
        else if (query.contains("london") || query.contains("uk")) destination = "London";
        else if (query.contains("new york") || query.contains("nyc")) destination = "New York";
        else if (query.contains("rome") || query.contains("italy")) destination = "Rome";
        else if (query.contains("paris") || query.contains("france")) destination = "Paris";
        
        // Extract duration
        int duration = 4;
        if (query.contains("3 days") || query.contains("three days")) duration = 3;
        else if (query.contains("5 days") || query.contains("five days")) duration = 5;
        else if (query.contains("week") && !query.contains("next week")) duration = 7;
        else if (query.contains("2 weeks")) duration = 14;
        
        // Calculate dates
        LocalDate departureDate = LocalDate.now().plusDays(7);
        LocalDate returnDate = departureDate.plusDays(duration);
        
        // Extract number of adults
        int adults = 1;
        if (query.contains("2 adults") || query.contains("two adults")) adults = 2;
        else if (query.contains("3 adults") || query.contains("three adults")) adults = 3;
        
        // Extract budget
        Double budget = null;
        if (query.contains("$")) {
            String[] parts = query.split("\\$");
            if (parts.length > 1) {
                try {
                    String budgetStr = parts[1].replaceAll("[^0-9.]", "").split("\\s")[0];
                    budget = Double.parseDouble(budgetStr);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        
        // Extract class
        String preferredClass = null;
        if (query.contains("business")) preferredClass = "BUSINESS";
        else if (query.contains("first")) preferredClass = "FIRST";
        else if (query.contains("economy")) preferredClass = "ECONOMY";
        
        // Build JSON response
        Map<String, Object> response = new HashMap<>();
        response.put("originLocation", "NYC");
        response.put("destinationLocation", destination);
        response.put("departureDate", departureDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        response.put("returnDate", returnDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        response.put("numberOfAdults", adults);
        response.put("numberOfChildren", 0);
        if (budget != null) response.put("maxBudget", budget);
        if (preferredClass != null) response.put("preferredClass", preferredClass);
        response.put("durationDays", duration);
        response.put("preferences", "Mock extracted preferences");
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return Mono.just(mapper.writeValueAsString(response));
        } catch (Exception e) {
            log.error("Error creating mock JSON", e);
            return Mono.just("{}");
        }
    }
    
    public Mono<String> generateRecommendations(String travelContext, List<String> flightOptions, List<String> hotelOptions) {
        log.info("MOCK: Generating recommendations");
        
        StringBuilder recommendation = new StringBuilder();
        recommendation.append("Based on your travel preferences, here are my recommendations:\n\n");
        
        if (!flightOptions.isEmpty()) {
            recommendation.append("FLIGHT RECOMMENDATIONS:\n");
            recommendation.append("I recommend the first flight option as it offers the best balance of price and convenience.\n");
            recommendation.append("It has reasonable pricing and good departure times.\n\n");
        }
        
        if (!hotelOptions.isEmpty()) {
            recommendation.append("HOTEL RECOMMENDATIONS:\n");
            recommendation.append("For your stay, I suggest the second hotel option.\n");
            recommendation.append("It provides excellent value with good amenities and a central location.\n");
            recommendation.append("The price is competitive and the ratings are excellent.\n\n");
        }
        
        recommendation.append("OVERALL:\n");
        recommendation.append("This itinerary offers a great balance of comfort, convenience, and value.\n");
        recommendation.append("The combination of flights and hotels should provide you with a memorable travel experience.");
        
        return Mono.just(recommendation.toString());
    }
}

