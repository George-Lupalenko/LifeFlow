package application.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.*;

/**
 * Mock Booking.com Client for testing without API keys
 * Enable with: mock.booking.enabled=true in application.properties
 */
@Service
@ConditionalOnProperty(name = "mock.booking.enabled", havingValue = "true", matchIfMissing = false)
@org.springframework.context.annotation.Primary
@Slf4j
public class MockBookingComClient implements HotelClient {
    
    public MockBookingComClient() {
        log.info("Using MOCK Booking.com Client - no real API calls will be made");
    }
    
    public Mono<Map<String, Object>> searchHotels(String city, String country,
                                                  LocalDate checkIn, LocalDate checkOut,
                                                  Integer adults, Integer rooms) {
        log.info("MOCK: Searching hotels in {} from {} to {}", city, checkIn, checkOut);
        
        return Mono.just(createMockHotelResponse(city, checkIn, checkOut, adults, rooms));
    }
    
    private Map<String, Object> createMockHotelResponse(String city, LocalDate checkIn, 
                                                        LocalDate checkOut, Integer adults, Integer rooms) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();
        
        // Create 4 mock hotel options
        String[] hotelNames = {
            "Grand " + city + " Hotel",
            "Central " + city + " Plaza",
            "Luxury " + city + " Suites",
            "Budget " + city + " Inn"
        };
        
        double[] prices = {150.0, 200.0, 120.0, 80.0};
        double[] ratings = {4.5, 4.8, 4.2, 3.8};
        
        for (int i = 0; i < 4; i++) {
            Map<String, Object> hotel = new HashMap<>();
            hotel.put("id", "MOCK-HOTEL-" + (i + 1));
            hotel.put("name", hotelNames[i]);
            hotel.put("city", city);
            hotel.put("country", "Unknown");
            hotel.put("address", "123 Main Street, " + city);
            hotel.put("rating", ratings[i]);
            
            // Price
            Map<String, Object> price = new HashMap<>();
            price.put("per_night", prices[i]);
            hotel.put("price", price);
            
            // Amenities
            String[] amenities = {
                "WiFi, Pool, Gym, Restaurant",
                "WiFi, Spa, Restaurant, Bar, Parking",
                "WiFi, Breakfast, Parking",
                "WiFi, Parking"
            };
            hotel.put("amenities", amenities[i]);
            
            results.add(hotel);
        }
        
        response.put("results", results);
        response.put("count", results.size());
        
        return response;
    }
}

