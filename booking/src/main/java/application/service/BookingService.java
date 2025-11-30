package application.service;

import application.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import application.model.BookingRecord;
import application.model.Itinerary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    
    private final BookingRepository bookingRepository;
    
    /**
     * Create a booking from an itinerary
     */
    @Transactional
    public BookingRecord createBooking(String userId, Itinerary itinerary, 
                                       String customerEmail, String customerPhone) {
        log.info("Creating booking for user {} and itinerary {}", userId, itinerary.getId());
        
        BookingRecord booking = new BookingRecord();
        booking.setUserId(userId);
        booking.setItinerary(itinerary);
        booking.setBookingReference(generateBookingReference());
        booking.setTotalAmount(itinerary.getTotalPrice());
        booking.setCurrency(itinerary.getCurrency());
        booking.setStatus(BookingRecord.BookingStatus.CONFIRMED);
        booking.setBookedAt(LocalDateTime.now());
        booking.setCustomerEmail(customerEmail);
        booking.setCustomerPhone(customerPhone);
        
        // Update itinerary status
        itinerary.setStatus(Itinerary.ItineraryStatus.BOOKED);
        
        return bookingRepository.save(booking);
    }
    
    /**
     * Get booking by ID
     */
    public Optional<BookingRecord> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }
    
    /**
     * Get booking by reference
     */
    public Optional<BookingRecord> getBookingByReference(String reference) {
        return bookingRepository.findByBookingReference(reference);
    }
    
    /**
     * Get all bookings for a user
     */
    public List<BookingRecord> getUserBookings(String userId) {
        return bookingRepository.findByUserId(userId);
    }
    
    /**
     * Cancel a booking
     */
    @Transactional
    public BookingRecord cancelBooking(Long bookingId, String reason) {
        Optional<BookingRecord> bookingOpt = bookingRepository.findById(bookingId);
        
        if (bookingOpt.isEmpty()) {
            throw new RuntimeException("Booking not found");
        }
        
        BookingRecord booking = bookingOpt.get();
        booking.setStatus(BookingRecord.BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(reason);
        
        // Update itinerary status
        if (booking.getItinerary() != null) {
            booking.getItinerary().setStatus(Itinerary.ItineraryStatus.CANCELLED);
        }
        
        return bookingRepository.save(booking);
    }
    
    /**
     * Generate unique booking reference
     */
    private String generateBookingReference() {
        return "BK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
