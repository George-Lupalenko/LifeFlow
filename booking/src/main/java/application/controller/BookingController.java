package application.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import application.model.BookingRecord;
import application.model.Itinerary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import application.repository.BookingRepository;
import application.repository.ItineraryRepository;
import application.service.BookingService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final ItineraryRepository itineraryRepository;
    
    /**
     * Create a booking
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createBooking(
            @RequestBody @Valid CreateBookingRequest request) {
        
        try {
            Itinerary itinerary = itineraryRepository.findById(request.getItineraryId())
                    .orElseThrow(() -> new RuntimeException("Itinerary not found"));
            
            BookingRecord booking = bookingService.createBooking(
                request.getUserId(),
                itinerary,
                request.getCustomerEmail(),
                request.getCustomerPhone()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("bookingId", booking.getId());
            response.put("bookingReference", booking.getBookingReference());
            response.put("status", booking.getStatus());
            response.put("totalAmount", booking.getTotalAmount());
            response.put("currency", booking.getCurrency());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Error creating booking", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create booking");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Get booking by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingRecord> getBooking(@PathVariable Long id) {
        return bookingService.getBookingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get booking by reference
     */
    @GetMapping("/reference/{reference}")
    public ResponseEntity<BookingRecord> getBookingByReference(@PathVariable String reference) {
        return bookingService.getBookingByReference(reference)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get all bookings for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingRecord>> getUserBookings(@PathVariable String userId) {
        List<BookingRecord> bookings = bookingService.getUserBookings(userId);
        return ResponseEntity.ok(bookings);
    }
    
    /**
     * Cancel a booking
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelBooking(
            @PathVariable Long id,
            @RequestBody(required = false) CancelBookingRequest request) {
        
        try {
            String reason = request != null && request.getReason() != null ? 
                request.getReason() : "Cancelled by user";
            
            BookingRecord booking = bookingService.cancelBooking(id, reason);
            
            Map<String, Object> response = new HashMap<>();
            response.put("bookingId", booking.getId());
            response.put("bookingReference", booking.getBookingReference());
            response.put("status", booking.getStatus());
            response.put("cancelledAt", booking.getCancelledAt());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error cancelling booking", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to cancel booking");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @Data
    static class CreateBookingRequest {
        @NotBlank
        private String userId;
        
        @NotNull
        private Long itineraryId;
        
        @Email
        @NotBlank
        private String customerEmail;
        
        private String customerPhone;
    }
    
    @Data
    static class CancelBookingRequest {
        private String reason;
    }
}
