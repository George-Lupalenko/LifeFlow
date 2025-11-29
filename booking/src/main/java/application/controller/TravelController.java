package application.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import application.model.Itinerary;
import application.model.TravelIntent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import application.repository.TravelIntentRepository;
import application.service.AiService;
import application.service.ItineraryService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/travel")
@RequiredArgsConstructor
@Slf4j
public class TravelController {
    
    private final TravelIntentRepository travelIntentRepository;
    private final ItineraryService itineraryService;
    private final AiService aiService;
    
    /**
     * Simple endpoint: Just provide natural language query and get best trip options
     * Example: "I want to visit Paris for 4 days"
     * This processes the request asynchronously and returns immediately with a status
     */
    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> searchTrips(
            @RequestBody @Valid SimpleTravelRequest request) {
        
        log.info("Received travel search request: {}", request.getQuery());
        
        try {
            // Step 1: Extract travel intent using AI (blocking call for simplicity)
            AiService.TravelIntentData intentData = aiService.extractTravelIntent(request.getQuery())
                    .block(); // Block here to get the data synchronously
            
            if (intentData == null || intentData.getDestinationLocation() == null || intentData.getDestinationLocation().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Could not extract travel destination from query");
                errorResponse.put("message", "Please provide a destination in your query, e.g., 'I want to visit Paris for 4 days'");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Step 2: Create TravelIntent entity
            TravelIntent travelIntent = new TravelIntent();
            travelIntent.setUserId(request.getUserId());
            travelIntent.setUserQuery(request.getQuery());
            travelIntent.setOriginLocation(intentData.getOriginLocation() != null && !intentData.getOriginLocation().isEmpty() 
                ? intentData.getOriginLocation() : "NYC"); // Default origin
            travelIntent.setDestinationLocation(intentData.getDestinationLocation());
            travelIntent.setDepartureDate(intentData.getDepartureDate() != null ? 
                intentData.getDepartureDate() : LocalDate.now().plusDays(7));
            travelIntent.setReturnDate(intentData.getReturnDate());
            travelIntent.setNumberOfAdults(intentData.getNumberOfAdults());
            travelIntent.setNumberOfChildren(intentData.getNumberOfChildren());
            travelIntent.setMaxBudget(intentData.getMaxBudget());
            travelIntent.setPreferredClass(intentData.getPreferredClass());
            travelIntent.setStatus(TravelIntent.IntentStatus.PROCESSING);
            
            // Save travel intent (use final variable for lambda)
            TravelIntent savedTravelIntent = travelIntentRepository.save(travelIntent);
            
            // Step 3: Generate itinerary with flights and hotels (async)
            CompletableFuture<Itinerary> itineraryFuture = itineraryService.createItinerary(savedTravelIntent)
                    .toFuture()
                    .thenApply(itinerary -> {
                        itinerary.setTravelIntent(savedTravelIntent);
                        return itineraryService.saveItinerary(itinerary);
                    })
                    .thenCompose(itinerary -> {
                        // Step 4: Generate AI recommendations
                        return aiService.generateRecommendations(savedTravelIntent, itinerary)
                                .toFuture()
                                .thenApply(recommendation -> {
                                    itinerary.setAiRecommendation(recommendation);
                                    return itineraryService.saveItinerary(itinerary);
                                });
                    })
                    .thenApply(itinerary -> {
                        savedTravelIntent.setStatus(TravelIntent.IntentStatus.COMPLETED);
                        savedTravelIntent.setProcessedAt(java.time.LocalDateTime.now());
                        travelIntentRepository.save(savedTravelIntent);
                        return itinerary;
                    });
            
            // Return immediately with processing status
            Map<String, Object> response = new HashMap<>();
            response.put("travelIntentId", savedTravelIntent.getId());
            response.put("status", "processing");
            response.put("message", "Your trip is being searched. Use GET /api/travel/intent/{id}/itinerary to check results.");
            
            // Extract key information that we already have
            Map<String, Object> extractedInfo = new HashMap<>();
            extractedInfo.put("origin", savedTravelIntent.getOriginLocation());
            extractedInfo.put("destination", savedTravelIntent.getDestinationLocation());
            extractedInfo.put("departureDate", savedTravelIntent.getDepartureDate());
            extractedInfo.put("returnDate", savedTravelIntent.getReturnDate());
            extractedInfo.put("adults", savedTravelIntent.getNumberOfAdults());
            extractedInfo.put("children", savedTravelIntent.getNumberOfChildren());
            response.put("extractedInfo", extractedInfo);
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            log.error("Error processing travel search", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to process travel search");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Create travel intent from natural language query (original endpoint with more control)
     */
    @PostMapping("/intent")
    public ResponseEntity<Map<String, Object>> createTravelIntent(
            @RequestBody @Valid TravelIntentRequest request) {
        
        log.info("Received travel intent request: {}", request.getUserQuery());
        
        TravelIntent travelIntent = new TravelIntent();
        travelIntent.setUserId(request.getUserId());
        travelIntent.setUserQuery(request.getUserQuery());
        travelIntent.setStatus(TravelIntent.IntentStatus.PENDING);
        
        // Use AI to extract structured data from natural language
        try {
            AiService.TravelIntentData intentData = aiService.extractTravelIntent(request.getUserQuery())
                    .block(); // Block to get data synchronously
            
            // Use AI-extracted data, but allow manual overrides
            travelIntent.setOriginLocation(
                request.getOriginLocation() != null ? request.getOriginLocation() : intentData.getOriginLocation());
            travelIntent.setDestinationLocation(
                request.getDestinationLocation() != null ? request.getDestinationLocation() : intentData.getDestinationLocation());
            travelIntent.setDepartureDate(
                request.getDepartureDate() != null ? request.getDepartureDate() : intentData.getDepartureDate());
            travelIntent.setReturnDate(
                request.getReturnDate() != null ? request.getReturnDate() : intentData.getReturnDate());
            travelIntent.setNumberOfAdults(
                request.getNumberOfAdults() != null ? request.getNumberOfAdults() : intentData.getNumberOfAdults());
            travelIntent.setNumberOfChildren(
                request.getNumberOfChildren() != null ? request.getNumberOfChildren() : intentData.getNumberOfChildren());
            travelIntent.setMaxBudget(
                request.getMaxBudget() != null ? request.getMaxBudget() : intentData.getMaxBudget());
            travelIntent.setPreferredClass(
                request.getPreferredClass() != null ? request.getPreferredClass() : intentData.getPreferredClass());
            
            // Save travel intent (don't reassign to keep it effectively final)
            travelIntentRepository.save(travelIntent);
            
            // Create itinerary asynchronously
            travelIntent.setStatus(TravelIntent.IntentStatus.PROCESSING);
            TravelIntent savedTravelIntent = travelIntentRepository.save(travelIntent);
            
            CompletableFuture<Itinerary> itineraryFuture = itineraryService.createItinerary(savedTravelIntent)
                    .toFuture()
                    .thenApply(itinerary -> {
                        itinerary.setTravelIntent(savedTravelIntent);
                        return itineraryService.saveItinerary(itinerary);
                    })
                    .thenApply(itinerary -> {
                        savedTravelIntent.setStatus(TravelIntent.IntentStatus.COMPLETED);
                        savedTravelIntent.setProcessedAt(java.time.LocalDateTime.now());
                        travelIntentRepository.save(savedTravelIntent);
                        return itinerary;
                    });
            
            Map<String, Object> response = new HashMap<>();
            response.put("travelIntentId", savedTravelIntent.getId());
            response.put("status", "processing");
            response.put("message", "Travel intent created. Itinerary is being generated.");
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception error) {
            log.error("Error processing travel intent", error);
            travelIntent.setStatus(TravelIntent.IntentStatus.FAILED);
            travelIntentRepository.save(travelIntent);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to process travel intent");
            errorResponse.put("message", error.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get travel intent by ID
     */
    @GetMapping("/intent/{id}")
    public ResponseEntity<TravelIntent> getTravelIntent(@PathVariable Long id) {
        return travelIntentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get itinerary for a travel intent
     */
    @GetMapping("/intent/{id}/itinerary")
    public ResponseEntity<Itinerary> getItinerary(@PathVariable Long id) {
        Optional<TravelIntent> travelIntentOpt = travelIntentRepository.findById(id);
        
        if (travelIntentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        TravelIntent travelIntent = travelIntentOpt.get();
        if (travelIntent.getItineraries() != null && !travelIntent.getItineraries().isEmpty()) {
            return ResponseEntity.ok(travelIntent.getItineraries().get(0));
        }
        
        return ResponseEntity.notFound().build();
    }
    
    @Data
    static class SimpleTravelRequest {
        @NotBlank
        private String userId;
        
        @NotBlank
        private String query; // Natural language query like "I want to visit Paris for 4 days"
    }
    
    @Data
    static class TravelIntentRequest {
        @NotBlank
        private String userId;
        
        @NotBlank
        private String userQuery;
        
        private String originLocation;
        private String destinationLocation;
        
        private LocalDate departureDate;
        private LocalDate returnDate;
        
        private Integer numberOfAdults = 1;
        private Integer numberOfChildren = 0;
        private Double maxBudget;
        private String preferredClass;
    }
}
