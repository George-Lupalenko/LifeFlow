package application.client;

import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Map;

/**
 * Interface for hotel search clients
 */
public interface HotelClient {
    Mono<Map<String, Object>> searchHotels(String city, String country,
                                          LocalDate checkIn, LocalDate checkOut,
                                          Integer adults, Integer rooms);
}

