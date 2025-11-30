package application.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.*;


@Service
@ConditionalOnProperty(name = "mock.amadeus.enabled", havingValue = "true", matchIfMissing = false)
@org.springframework.context.annotation.Primary
@Slf4j
public class MockAmadeusClient implements FlightClient {

    public MockAmadeusClient() {
        log.info("Using MOCK Amadeus Client - no real API calls will be made");
    }

    @Override
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
        int adultCount = adults != null ? adults : 1;


        for (int i = 0; i < 3; i++) {
            Map<String, Object> flight = new HashMap<>();
            flight.put("id", "MOCK-FLIGHT-" + (i + 1));

            List<Map<String, Object>> itineraries = new ArrayList<>();
            Map<String, Object> itinerary = new HashMap<>();
            List<Map<String, Object>> segments = new ArrayList<>();
            Map<String, Object> segment = new HashMap<>();

            String airline = switch (i) {
                case 0 -> "AA";
                case 1 -> "DL";
                default -> "UA";
            };
            String flightNumber = String.valueOf(1000 + i * 123);

            segment.put("carrierCode", airline);
            segment.put("number", flightNumber);

            Map<String, Object> dep = new HashMap<>();
            dep.put("iataCode", origin != null && !origin.isEmpty() ? origin : "NYC");
            dep.put("at", departureDate.atTime(10 + i * 2, 0).toString());
            segment.put("departure", dep);

            Map<String, Object> arr = new HashMap<>();
            arr.put("iataCode", destination != null && !destination.isEmpty() ? destination : "PAR");
            arr.put("at", departureDate.atTime(18 + i * 2, 30).toString());
            segment.put("arrival", arr);

            segments.add(segment);
            itinerary.put("segments", segments);
            itinerary.put("duration", "PT" + (8 + i) + "H" + (30 + i * 10) + "M");
            itineraries.add(itinerary);

            flight.put("itineraries", itineraries);

            Map<String, Object> price = new HashMap<>();
            double basePrice = 400 + i * 150;
            price.put("total", String.format("%.2f", basePrice * adultCount));
            price.put("currency", "USD");
            flight.put("price", price);

            List<Map<String, Object>> travelerPricings = new ArrayList<>();
            Map<String, Object> travelerPricing = new HashMap<>();
            String cabinClass = switch (i) {
                case 0 -> "ECONOMY";
                case 1 -> "BUSINESS";
                default -> "FIRST";
            };
            travelerPricing.put("cabin", cabinClass);
            travelerPricings.add(travelerPricing);
            flight.put("travelerPricings", travelerPricings);

            data.add(flight);
        }

        response.put("data", data);
        response.put("meta", Map.of("count", data.size()));
        return response;
    }
}
