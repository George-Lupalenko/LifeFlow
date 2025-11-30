package application.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String userId;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id")
    private Itinerary itinerary;
    
    @Column(nullable = false)
    private String bookingReference; // Unique booking reference
    
    @Column(nullable = false)
    private BigDecimal totalAmount;
    
    private String currency = "USD";
    
    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.CONFIRMED;
    
    @Column(nullable = false)
    private LocalDateTime bookedAt = LocalDateTime.now();
    
    private LocalDateTime cancelledAt;
    
    @Column(length = 2000)
    private String cancellationReason;
    
    @Column(length = 1000)
    private String customerEmail;
    
    @Column(length = 1000)
    private String customerPhone;
    
    @Column(length = 2000)
    private String providerConfirmation; // Confirmation from external provider
    
    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED, REFUNDED
    }
}
