package application.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.*;

/**
 * Mock Amadeus Client for testing without API keys
 * Enable with: mock.amadeus.enabled=true in application.properties
 */
@Service
@ConditionalOnProperty(name = "mock.amadeus.enabled", havingValue = "true", matchIfMissing = false)
@org.springframework.context.annotation.Primary
@Slf4j
public class MockAmadeusClient implements FlightClient {
    
    public MockAmadeusClient() {
        log.info("Using MOCK Amadeus Client - no real API calls will be made");
    }
    
    public Mono<Map<String, Object>> searchFlights(String origin, String destination, 
                                                    LocalDate departureDate, LocalDate returnDate,
                                                    Integer adults, Integer children) {
        log.info("MOCK: Searching flights from {} to {} on {}", origin, destination, departureDate);
        
        return Mono.just(createMockFlightResponse(origin, destination, departureDate, returnDate, adults, children));
    }
    
    private Map<String, Object> createMockFlightResponse(String origin, String destination, 
                                                         LocalDate departureDate, LocalDate returnDate,
                                                         Integer adults, Integer children) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();
        
        // Create 3 mock flight options
        for (int i = 0; i < 3; i++) {
            Map<String, Object> flight = new HashMap<>();
            flight.put("id", "MOCK-FLIGHT-" + (i + 1));
            
            // Create itinerary
            List<Map<String, Object>> itineraries = new ArrayList<>();
            Map<String, Object> itinerary = new HashMap<>();
            
            // Create segments
            List<Map<String, Object>> segments = new ArrayList<>();
            Map<String, Object> segment = new HashMap<>();
            
            String[] airlines = {"AA", "DL", "UA"};
            String[] flightNumbers = {"1234", "5678", "9012"};
            
            segment.put("carrierCode", airlines[i]);
            segment.put("number", flightNumbers[i]);
            
            // Departure
            Map<String, Object> departure = new HashMap<>();
            departure.put("iataCode", origin);
            departure.put("at", departureDate.atTime(10 + i * 2, 0).format(
                java.time.format.DateTimeFormatter.ISO_DATE_TIME));
            segment.put("departure", departure);
            
            // Arrival
            Map<String, Object> arrival = new HashMap<>();
            arrival.put("iataCode", destination);
            arrival.put("at", departureDate.atTime(10 + i * 2 + 8, 30).format(
                java.time.format.DateTimeFormatter.ISO_DATE_TIME));
            segment.put("arrival", arrival);
            
            segments.add(segment);
            itinerary.put("segments", segments);
            itinerary.put("duration", "PT" + (8 + i) + "H" + (30 + i * 10) + "M");
            itineraries.add(itinerary);
            
            flight.put("itineraries", itineraries);
            
            // Price
            Map<String, Object> price = new HashMap<>();
            double basePrice = 500.0 + (i * 150.0);
            price.put("total", String.format("%.2f", basePrice * (adults != null ? adults : 1)));
            price.put("currency", "USD");
            flight.put("price", price);
            
            // Traveler pricing
            List<Map<String, Object>> travelerPricings = new ArrayList<>();
            Map<String, Object> travelerPricing = new HashMap<>();
            String[] classes = {"ECONOMY", "BUSINESS", "FIRST"};
            travelerPricing.put("cabin", classes[i]);
            travelerPricings.add(travelerPricing);
            flight.put("travelerPricings", travelerPricings);
            
            data.add(flight);
        }
        
        response.put("data", data);
        response.put("meta", Map.of("count", data.size()));
        
        return response;
    }
}

