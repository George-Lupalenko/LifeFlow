package application.repository;

import application.model.BookingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<BookingRecord, Long> {
    
    List<BookingRecord> findByUserId(String userId);
    
    Optional<BookingRecord> findByBookingReference(String bookingReference);
    
    List<BookingRecord> findByStatus(BookingRecord.BookingStatus status);
    
    List<BookingRecord> findByUserIdAndStatus(String userId, BookingRecord.BookingStatus status);
}
