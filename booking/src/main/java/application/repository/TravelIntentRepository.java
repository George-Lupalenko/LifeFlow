package application.repository;

import application.model.TravelIntent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravelIntentRepository extends JpaRepository<TravelIntent, Long> {
    
    List<TravelIntent> findByUserId(String userId);
    
    List<TravelIntent> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<TravelIntent> findByStatus(TravelIntent.IntentStatus status);
    
    Optional<TravelIntent> findById(Long id);
}

