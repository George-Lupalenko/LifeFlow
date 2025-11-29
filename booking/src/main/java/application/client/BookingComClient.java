package application.client;

import application.config.HotelApiConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "mock.booking.enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class BookingComClient implements HotelClient {
    
    private final WebClient webClient;
    private final HotelApiConfig config;
    
    public BookingComClient(WebClient bookingComWebClient, HotelApiConfig config) {
        this.webClient = bookingComWebClient;
        this.config = config;
    }
    
    /**
     * Search for hotels
     * Note: This is a simplified implementation. 
     * Booking.com API requires specific authentication and endpoint structure.
     */
    public Mono<Map<String, Object>> searchHotels(String city, String country,
                                                   LocalDate checkIn, LocalDate checkOut,
                                                   Integer adults, Integer rooms) {
        // Booking.com API endpoint structure varies
        // This is a placeholder implementation
        String url = String.format("/getHotelAvailabilityV2?" +
                "city=%s&country=%s&checkin=%s&checkout=%s&adults=%d&rooms=%d",
                city, country,
                checkIn.format(DateTimeFormatter.ISO_LOCAL_DATE),
                checkOut.format(DateTimeFormatter.ISO_LOCAL_DATE),
                adults != null ? adults : 1,
                rooms != null ? rooms : 1);
        
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnError(error -> {
                    log.error("Error searching hotels from Booking.com", error);
                    // Return empty result on error
                })
                .onErrorReturn(Map.of("results", java.util.List.of()));
    }
    
    /**
     * Get hotel details
     */
    public Mono<Map<String, Object>> getHotelDetails(String hotelId) {
        String url = "/getHotelDetails?hotel_id=" + hotelId;
        
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnError(error -> log.error("Error getting hotel details", error))
                .onErrorReturn(Map.of());
    }
}
