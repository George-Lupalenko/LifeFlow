package application.service;

import application.client.AiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import application.model.Itinerary;
import application.model.TravelIntent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {
    
    private final AiClient aiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Extract structured travel intent from natural language query
     * Returns a TravelIntentData object with extracted information
     */
    public Mono<TravelIntentData> extractTravelIntent(String userQuery) {
        log.info("Extracting travel intent from query: {}", userQuery);
        
        String prompt = String.format(
            "Extract travel information from this query and return ONLY valid JSON (no markdown, no code blocks, just JSON): " +
            "{\"originLocation\": \"city name or IATA code (e.g., NYC, New York, or user's current location if not specified)\", " +
            "\"destinationLocation\": \"city name or IATA code\", " +
            "\"departureDate\": \"YYYY-MM-DD (calculate from 'in X days' or 'on date' or use today+7 if not specified)\", " +
            "\"returnDate\": \"YYYY-MM-DD (calculate from duration like '4 days' or null for one-way)\", " +
            "\"numberOfAdults\": number, " +
            "\"numberOfChildren\": number, " +
            "\"maxBudget\": number or null, " +
            "\"preferredClass\": \"ECONOMY\" or \"BUSINESS\" or \"FIRST\" or null, " +
            "\"durationDays\": number (extract from phrases like '4 days', 'a week'), " +
            "\"preferences\": \"any special requirements mentioned\"}. " +
            "Today's date is %s. " +
            "If user says 'visit X for Y days', calculate returnDate = departureDate + Y days. " +
            "User query: %s",
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
            userQuery
        );
        
        // Call OpenAI with the detailed prompt
        return aiClient.extractTravelIntent(prompt)
                .map(jsonText -> {
                    try {
                        // Clean JSON text - remove markdown code blocks if present
                        String cleanJson = jsonText.trim();
                        if (cleanJson.startsWith("```json")) {
                            cleanJson = cleanJson.substring(7);
                        }
                        if (cleanJson.startsWith("```")) {
                            cleanJson = cleanJson.substring(3);
                        }
                        if (cleanJson.endsWith("```")) {
                            cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
                        }
                        cleanJson = cleanJson.trim();
                        
                        // Parse JSON
                        Map<String, Object> data = objectMapper.readValue(cleanJson, Map.class);
                        
                        TravelIntentData intentData = new TravelIntentData();
                        intentData.setOriginLocation((String) data.getOrDefault("originLocation", ""));
                        intentData.setDestinationLocation((String) data.getOrDefault("destinationLocation", ""));
                        
                        // Parse dates
                        String depDateStr = (String) data.get("departureDate");
                        if (depDateStr != null && !depDateStr.isEmpty()) {
                            intentData.setDepartureDate(LocalDate.parse(depDateStr));
                        }
                        
                        String retDateStr = (String) data.get("returnDate");
                        if (retDateStr != null && !retDateStr.isEmpty() && !retDateStr.equals("null")) {
                            intentData.setReturnDate(LocalDate.parse(retDateStr));
                        }
                        
                        // Handle duration - if returnDate is null but durationDays is provided
                        if (intentData.getReturnDate() == null && intentData.getDepartureDate() != null) {
                            Object durationObj = data.get("durationDays");
                            if (durationObj != null) {
                                int duration = durationObj instanceof Number ? 
                                    ((Number) durationObj).intValue() : 
                                    Integer.parseInt(durationObj.toString());
                                intentData.setReturnDate(intentData.getDepartureDate().plusDays(duration));
                            }
                        }
                        
                        intentData.setNumberOfAdults(data.get("numberOfAdults") != null ? 
                            ((Number) data.get("numberOfAdults")).intValue() : 1);
                        intentData.setNumberOfChildren(data.get("numberOfChildren") != null ? 
                            ((Number) data.get("numberOfChildren")).intValue() : 0);
                        
                        Object budgetObj = data.get("maxBudget");
                        if (budgetObj != null) {
                            intentData.setMaxBudget(budgetObj instanceof Number ? 
                                ((Number) budgetObj).doubleValue() : 
                                Double.parseDouble(budgetObj.toString()));
                        }
                        
                        intentData.setPreferredClass((String) data.get("preferredClass"));
                        intentData.setPreferences((String) data.get("preferences"));
                        
                        log.info("Extracted travel intent: {}", intentData);
                        return intentData;
                        
                    } catch (Exception e) {
                        log.error("Error parsing AI-extracted JSON: {}", jsonText, e);
                        // Return default values
                        TravelIntentData defaultData = new TravelIntentData();
                        defaultData.setNumberOfAdults(1);
                        defaultData.setNumberOfChildren(0);
                        defaultData.setDepartureDate(LocalDate.now().plusDays(7));
                        return defaultData;
                    }
                })
                .onErrorReturn(createDefaultTravelIntent());
    }
    
    private TravelIntentData createDefaultTravelIntent() {
        TravelIntentData data = new TravelIntentData();
        data.setNumberOfAdults(1);
        data.setNumberOfChildren(0);
        data.setDepartureDate(LocalDate.now().plusDays(7));
        return data;
    }
    
    /**
     * Generate personalized recommendations for an itinerary
     */
    public Mono<String> generateRecommendations(TravelIntent travelIntent, Itinerary itinerary) {
        String context = String.format(
            "Travel from %s to %s, departing %s%s. Budget: %s, Class: %s",
            travelIntent.getOriginLocation(),
            travelIntent.getDestinationLocation(),
            travelIntent.getDepartureDate(),
            travelIntent.getReturnDate() != null ? ", returning " + travelIntent.getReturnDate() : "",
            travelIntent.getMaxBudget() != null ? "$" + travelIntent.getMaxBudget() : "Not specified",
            travelIntent.getPreferredClass() != null ? travelIntent.getPreferredClass() : "Not specified"
        );
        
        List<String> flightOptions = itinerary.getFlightOptions() != null ?
            itinerary.getFlightOptions().stream()
                .map(f -> String.format("%s %s: $%.2f (%s to %s, %d stops)", 
                    f.getAirline(), f.getFlightNumber(), f.getPrice(),
                    f.getOriginAirport(), f.getDestinationAirport(), f.getStops()))
                .collect(Collectors.toList()) : List.of();
        
        List<String> hotelOptions = itinerary.getHotelOptions() != null ?
            itinerary.getHotelOptions().stream()
                .map(h -> String.format("%s: $%.2f/night (Rating: %.1f, %d nights total: $%.2f)", 
                    h.getHotelName(), h.getPricePerNight(), 
                    h.getRating() != null ? h.getRating() : 0.0,
                    h.getNumberOfNights(), h.getTotalPrice()))
                .collect(Collectors.toList()) : List.of();
        
        log.info("Generating AI recommendations for itinerary {}", itinerary.getId());
        return aiClient.generateRecommendations(context, flightOptions, hotelOptions);
    }
    
    /**
     * Rank and select best trip options from multiple itineraries
     */
    public Mono<String> rankTripOptions(String userQuery, List<Itinerary> itineraries) {
        StringBuilder optionsText = new StringBuilder();
        for (int i = 0; i < itineraries.size(); i++) {
            Itinerary it = itineraries.get(i);
            optionsText.append(String.format("\nOption %d:\n", i + 1));
            optionsText.append(String.format("Total Price: $%.2f\n", it.getTotalPrice()));
            if (it.getFlightOptions() != null && !it.getFlightOptions().isEmpty()) {
                optionsText.append("Flights: ").append(it.getFlightOptions().size()).append(" options\n");
            }
            if (it.getHotelOptions() != null && !it.getHotelOptions().isEmpty()) {
                optionsText.append("Hotels: ").append(it.getHotelOptions().size()).append(" options\n");
            }
        }
        
        String prompt = String.format(
            "User wants: %s\n\n" +
            "Available trip options:%s\n\n" +
            "Rank these options from best to worst and explain why. " +
            "Consider: price, convenience, quality, duration, and how well they match the user's request. " +
            "Return a JSON array with rankings: [{\"rank\": 1, \"itineraryIndex\": 0, \"reason\": \"explanation\"}, ...]",
            userQuery, optionsText.toString()
        );
        
        return aiClient.extractTravelIntent(prompt)
                .map(ranking -> {
                    // This would parse the ranking JSON
                    return ranking;
                });
    }
    
    @Data
    public static class TravelIntentData {
        private String originLocation;
        private String destinationLocation;
        private LocalDate departureDate;
        private LocalDate returnDate;
        private Integer numberOfAdults = 1;
        private Integer numberOfChildren = 0;
        private Double maxBudget;
        private String preferredClass;
        private String preferences;
    }
}
