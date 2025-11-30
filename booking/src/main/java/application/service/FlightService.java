package application.service;

import application.client.FlightClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import application.model.FlightOption;
import application.model.Itinerary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlightService {

    private final FlightClient flightClient;

    /**
     * Search for flights and convert to FlightOption entities
     */
    public Mono<List<FlightOption>> searchFlights(String origin, String destination,
                                                  java.time.LocalDate departureDate,
                                                  java.time.LocalDate returnDate,
                                                  Integer adults, Integer children) {
        log.info("Searching flights: {} -> {}, departing {}", origin, destination, departureDate);

        return flightClient.searchFlights(origin, destination, departureDate, returnDate, adults, children)
                .map(response -> {
                    List<FlightOption> options = new ArrayList<>();

                    try {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");

                        if (data != null) {
                            for (Map<String, Object> offer : data) {
                                FlightOption option = parseFlightOffer(offer);
                                if (option != null) {
                                    options.add(option);
                                    log.info("Parsed flight: {} {} from {} to {} at {}",
                                            option.getAirline(),
                                            option.getFlightNumber(),
                                            option.getOriginAirport(),
                                            option.getDestinationAirport(),
                                            option.getDepartureTime());
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error parsing flight offers", e);
                    }

                    return options;
                })
                .onErrorReturn(new ArrayList<>());
    }

    /**
     * Parse flight offer into FlightOption entity
     */
    private FlightOption parseFlightOffer(Map<String, Object> offer) {
        try {
            FlightOption option = new FlightOption();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itineraries = (List<Map<String, Object>>) offer.get("itineraries");
            if (itineraries == null || itineraries.isEmpty()) {
                return null;
            }

            Map<String, Object> firstItinerary = itineraries.get(0); // <-- исправлено
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> segments = (List<Map<String, Object>>) firstItinerary.get("segments");

            if (segments != null && !segments.isEmpty()) {
                Map<String, Object> firstSegment = segments.get(0); // <-- исправлено
                Map<String, Object> lastSegment = segments.get(segments.size() - 1);

                option.setAirline((String) firstSegment.get("carrierCode"));
                option.setFlightNumber((String) firstSegment.get("number"));
                Map<String, Object> dep = (Map<String, Object>) firstSegment.get("departure");
                Map<String, Object> arr = (Map<String, Object>) lastSegment.get("arrival");

                option.setOriginAirport((String) dep.get("iataCode"));
                option.setDestinationAirport((String) arr.get("iataCode"));

                String depTime = (String) dep.get("at");
                String arrTime = (String) arr.get("at");

                option.setDepartureTime(LocalDateTime.parse(depTime, DateTimeFormatter.ISO_DATE_TIME));
                option.setArrivalTime(LocalDateTime.parse(arrTime, DateTimeFormatter.ISO_DATE_TIME));

                String duration = (String) firstItinerary.get("duration");
                option.setDurationMinutes(parseDuration(duration));
                option.setStops(segments.size() - 1);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> price = (Map<String, Object>) offer.get("price");
            if (price != null) {
                option.setPrice(BigDecimal.valueOf(Double.parseDouble(price.get("total").toString())));
                option.setCurrency((String) price.get("currency"));
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> travelerPricings = (List<Map<String, Object>>) offer.get("travelerPricings");
            if (travelerPricings != null && !travelerPricings.isEmpty()) {
                String cabinClass = (String) travelerPricings.get(0).get("cabin");
                option.setCabinClass(cabinClass != null ? cabinClass : "ECONOMY");
            }

            option.setProviderId((String) offer.get("id"));
            option.setCreatedAt(LocalDateTime.now());

            return option;
        } catch (Exception e) {
            log.error("Error parsing flight offer", e);
            return null;
        }
    }

    private Integer parseDuration(String duration) {
        // Parse PT14H30M format to minutes
        try {
            duration = duration.replace("PT", "");
            int hours = 0;
            int minutes = 0;

            if (duration.contains("H")) {
                String[] parts = duration.split("H");
                hours = Integer.parseInt(parts[0]);
                duration = parts.length > 1 ? parts[1] : "";
            }

            if (duration.contains("M")) {
                minutes = Integer.parseInt(duration.replace("M", ""));
            }

            return hours * 60 + minutes;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Add flight options to an itinerary
     */
    public void addFlightsToItinerary(Itinerary itinerary, List<FlightOption> flightOptions) {
        if (flightOptions != null) {
            flightOptions.forEach(option -> option.setItinerary(itinerary));
            itinerary.setFlightOptions(flightOptions);
        }
    }
}
