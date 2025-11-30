package application.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "flight_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id")
    @JsonBackReference(value = "itinerary-flights")
    private Itinerary itinerary;

    @Column(nullable = false)
    private String airline;

    @Column(nullable = false)
    private String flightNumber;

    @Column(nullable = false)
    private String originAirport;

    @Column(nullable = false)
    private String destinationAirport;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Column(nullable = false)
    private LocalDateTime arrivalTime;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private BigDecimal price;

    private String currency = "USD";

    @Column(nullable = false)
    private String cabinClass;

    private Integer stops = 0;

    @Column(length = 500)
    private String providerId;

    @Column(length = 1000)
    private String providerData;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
