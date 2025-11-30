package application.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    private String userQuery;

    @Column(nullable = false)
    private String originLocation;

    @Column
    private String originCountry;

    @Column
    private String destinationCountry;

    @Column(nullable = false)
    private String destinationLocation;

    @Column(nullable = false)
    private LocalDate departureDate;

    private LocalDate returnDate;

    private Integer numberOfAdults = 1;

    private Integer numberOfChildren = 0;

    private Double maxBudget;

    private String preferredClass;

    @Column(length = 2000)
    private String aiExtractedPreferences;

    @Enumerated(EnumType.STRING)
    private IntentStatus status = IntentStatus.PENDING;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime processedAt;

    @OneToMany(mappedBy = "travelIntent", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "intent-itineraries")
    private List<Itinerary> itineraries;

    public enum IntentStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}
