package application.client;

import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Map;

/**
 * Interface for flight search clients
 */
public interface FlightClient {
    Mono<Map<String, Object>> searchFlights(String origin, String destination,
                                             LocalDate departureDate, LocalDate returnDate,
                                             Integer adults, Integer children);
}

