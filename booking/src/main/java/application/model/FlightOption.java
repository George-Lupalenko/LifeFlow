package application.model;

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
    private Itinerary itinerary;
    
    @Column(nullable = false)
    private String airline;
    
    @Column(nullable = false)
    private String flightNumber;
    
    @Column(nullable = false)
    private String originAirport; // IATA code
    
    @Column(nullable = false)
    private String destinationAirport; // IATA code
    
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
    private String cabinClass; // ECONOMY, BUSINESS, FIRST
    
    private Integer stops = 0;
    
    @Column(length = 500)
    private String providerId; // ID from Amadeus API
    
    @Column(length = 1000)
    private String providerData; // JSON string of full response from provider
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
