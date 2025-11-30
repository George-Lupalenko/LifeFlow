package application.service;

import application.model.Itinerary;
import application.model.TravelIntent;
import application.repository.ItineraryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItineraryService {

    private final AiService aiService;
    private final ItineraryRepository itineraryRepository;

    @Transactional
    public Mono<Itinerary> createItinerary(TravelIntent travelIntent) {
        log.info("Creating mock itinerary for travel intent {}", travelIntent.getId());

        Itinerary itinerary = new Itinerary();
        itinerary.setTravelIntent(travelIntent);
        itinerary.setStatus(Itinerary.ItineraryStatus.DRAFT);

        // Generate AI recommendation with mock flights and hotels
        String aiRecommendation = aiService.generateTravelResponse(travelIntent.getUserQuery());
        itinerary.setAiRecommendation(aiRecommendation);

        // Save itinerary
        Itinerary savedItinerary = itineraryRepository.save(itinerary);
        return Mono.just(savedItinerary);
    }

    public Mono<Itinerary> getItineraryById(Long id) {
        return Mono.justOrEmpty(itineraryRepository.findById(id));
    }

    @Transactional
    public Itinerary saveItinerary(Itinerary itinerary) {
        return itineraryRepository.save(itinerary);
    }
}
