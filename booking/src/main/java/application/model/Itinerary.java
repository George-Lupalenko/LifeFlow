package application.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "itineraries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Itinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_intent_id", nullable = false)
    @JsonBackReference(value = "intent-itineraries")
    private TravelIntent travelIntent;

    @OneToMany(mappedBy = "itinerary",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JsonManagedReference(value = "itinerary-flights")
    private List<FlightOption> flightOptions;


    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference(value = "itinerary-hotels")
    private List<HotelOption> hotelOptions;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    private String currency = "USD";

    @Column(length = 2000)
    private String aiRecommendation;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private ItineraryStatus status = ItineraryStatus.DRAFT;

    public enum ItineraryStatus {
        DRAFT, SELECTED, BOOKED, CANCELLED
    }
}
