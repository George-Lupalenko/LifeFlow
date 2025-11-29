package application.repository;

import application.model.Itinerary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {
    
    List<Itinerary> findByTravelIntentId(Long travelIntentId);
    
    Optional<Itinerary> findById(Long id);
}

