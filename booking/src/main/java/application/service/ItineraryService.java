package application.service;

import application.model.FlightOption;
import application.model.HotelOption;
import application.model.Itinerary;
import application.model.TravelIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import application.repository.ItineraryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItineraryService {
    
    private final FlightService flightService;
    private final HotelService hotelService;
    private final AiService aiService;
    private final ItineraryRepository itineraryRepository;
    
    /**
     * Create itinerary from travel intent
     */
    @Transactional
    public Mono<Itinerary> createItinerary(TravelIntent travelIntent) {
        log.info("Creating itinerary for travel intent {}", travelIntent.getId());
        
        // Search flights
        Mono<List<FlightOption>> flightsMono = flightService.searchFlights(
            travelIntent.getOriginLocation(),
            travelIntent.getDestinationLocation(),
            travelIntent.getDepartureDate(),
            travelIntent.getReturnDate(),
            travelIntent.getNumberOfAdults(),
            travelIntent.getNumberOfChildren()
        );
        
        // Search hotels (if destination is provided)
        Mono<List<HotelOption>> hotelsMono = Mono.just(List.<HotelOption>of());
        if (travelIntent.getDestinationLocation() != null) {
            LocalDate checkIn = travelIntent.getDepartureDate();
            LocalDate checkOut = travelIntent.getReturnDate() != null ? 
                travelIntent.getReturnDate() : 
                travelIntent.getDepartureDate().plusDays(1);
            
            hotelsMono = hotelService.searchHotels(
                travelIntent.getDestinationLocation(),
                "", // Country would need to be extracted or provided
                checkIn,
                checkOut,
                travelIntent.getNumberOfAdults(),
                1 // Default to 1 room
            );
        }
        
        // Combine results
        return Mono.zip(flightsMono, hotelsMono)
                .map(tuple -> {
                    List<FlightOption> flights = tuple.getT1();
                    List<HotelOption> hotels = tuple.getT2();
                    
                    // Create itinerary inside lambda
                    Itinerary itinerary = new Itinerary();
                    itinerary.setTravelIntent(travelIntent);
                    itinerary.setStatus(Itinerary.ItineraryStatus.DRAFT);
                    
                    // Add flights and hotels to itinerary
                    flightService.addFlightsToItinerary(itinerary, flights);
                    hotelService.addHotelsToItinerary(itinerary, hotels);
                    
                    // Calculate total price
                    BigDecimal totalPrice = BigDecimal.ZERO;
                    if (flights != null) {
                        totalPrice = totalPrice.add(
                            flights.stream()
                                .map(FlightOption::getPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                        );
                    }
                    if (hotels != null) {
                        totalPrice = totalPrice.add(
                            hotels.stream()
                                .map(HotelOption::getTotalPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                        );
                    }
                    itinerary.setTotalPrice(totalPrice);
                    
                    return itinerary;
                })
                .flatMap(itinerary -> {
                    // Generate AI recommendations
                    return aiService.generateRecommendations(travelIntent, itinerary)
                            .map(recommendation -> {
                                itinerary.setAiRecommendation(recommendation);
                                return itinerary;
                            });
                });
    }
    
    /**
     * Get itinerary by ID
     */
    public Optional<Itinerary> getItineraryById(Long id) {
        return itineraryRepository.findById(id);
    }
    
    /**
     * Save itinerary
     */
    @Transactional
    public Itinerary saveItinerary(Itinerary itinerary) {
        return itineraryRepository.save(itinerary);
    }
}
