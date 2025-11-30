package application.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hotel_options")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id")
    @JsonBackReference(value = "itinerary-hotels")
    private Itinerary itinerary;

    @Column(nullable = false)
    private String hotelName;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    @Column(length = 1000)
    private String address;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = false)
    private Integer numberOfNights;

    @Column(nullable = false)
    private Integer numberOfRooms = 1;

    @Column(nullable = false)
    private Integer numberOfGuests;

    @Column(nullable = false)
    private BigDecimal pricePerNight;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    private String currency = "USD";

    private Double rating;

    @Column(length = 2000)
    private String amenities;

    @Column(length = 500)
    private String providerId;

    @Column(length = 2000)
    private String providerData;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
