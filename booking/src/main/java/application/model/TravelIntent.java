package application.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "travel_intents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TravelIntent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @Column(nullable = false, length = 1000)
    private String userQuery; // Natural language query from user
    
    @Column(nullable = false)
    private String originLocation; // IATA code or city name
    
    @Column(nullable = false)
    private String destinationLocation; // IATA code or city name
    
    @Column(nullable = false)
    private LocalDate departureDate;
    
    private LocalDate returnDate; // Optional for round trips
    
    private Integer numberOfAdults = 1;
    
    private Integer numberOfChildren = 0;
    
    private Double maxBudget; // Optional budget constraint
    
    private String preferredClass; // ECONOMY, BUSINESS, FIRST
    
    @Column(length = 2000)
    private String aiExtractedPreferences; // JSON string of AI-extracted preferences
    
    @Enumerated(EnumType.STRING)
    private IntentStatus status = IntentStatus.PENDING;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime processedAt;
    
    @OneToMany(mappedBy = "travelIntent", cascade = CascadeType.ALL)
    private List<Itinerary> itineraries;
    
    public enum IntentStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}
