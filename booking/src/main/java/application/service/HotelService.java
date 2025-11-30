package application.service;

import application.client.HotelClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import application.model.HotelOption;
import application.model.Itinerary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelService {
    
    private final HotelClient hotelClient;
    
    /**
     * Search for hotels and convert to HotelOption entities
     */
    public Mono<List<HotelOption>> searchHotels(String city, String country,
                                                LocalDate checkIn, LocalDate checkOut,
                                                Integer adults, Integer rooms) {
        log.info("Searching hotels in {} for dates {} to {}", city, checkIn, checkOut);
        
        return hotelClient.searchHotels(city, country, checkIn, checkOut, adults, rooms)
                .map(response -> {
                    List<HotelOption> options = new ArrayList<>();
                    
                    try {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                        
                        if (results != null) {
                            for (Map<String, Object> hotel : results) {
                                HotelOption option = parseHotelOffer(hotel, checkIn, checkOut, adults, rooms);
                                if (option != null) {
                                    options.add(option);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error parsing hotel offers", e);
                    }
                    
                    return options;
                })
                .onErrorReturn(new ArrayList<>());
    }
    
    /**
     * Parse Booking.com hotel offer into HotelOption entity
     */
    private HotelOption parseHotelOffer(Map<String, Object> hotel, LocalDate checkIn, 
                                       LocalDate checkOut, Integer adults, Integer rooms) {
        try {
            HotelOption option = new HotelOption();
            
            option.setHotelName((String) hotel.getOrDefault("name", "Unknown Hotel"));
            option.setCity((String) hotel.getOrDefault("city", ""));
            option.setCountry((String) hotel.getOrDefault("country", ""));
            option.setAddress((String) hotel.getOrDefault("address", ""));
            
            option.setCheckInDate(checkIn);
            option.setCheckOutDate(checkOut);
            option.setNumberOfNights((int) ChronoUnit.DAYS.between(checkIn, checkOut));
            option.setNumberOfRooms(rooms != null ? rooms : 1);
            option.setNumberOfGuests(adults != null ? adults : 1);
            
            // Parse price - structure may vary based on API
            Object priceObj = hotel.get("price");
            if (priceObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> price = (Map<String, Object>) priceObj;
                Double pricePerNight = Double.parseDouble(price.getOrDefault("per_night", "0").toString());
                option.setPricePerNight(BigDecimal.valueOf(pricePerNight));
                option.setTotalPrice(BigDecimal.valueOf(pricePerNight * option.getNumberOfNights()));
            } else if (priceObj != null) {
                Double price = Double.parseDouble(priceObj.toString());
                option.setPricePerNight(BigDecimal.valueOf(price));
                option.setTotalPrice(BigDecimal.valueOf(price * option.getNumberOfNights()));
            } else {
                option.setPricePerNight(BigDecimal.ZERO);
                option.setTotalPrice(BigDecimal.ZERO);
            }
            
            option.setRating(hotel.get("rating") != null ? 
                Double.parseDouble(hotel.get("rating").toString()) : null);
            
            option.setProviderId((String) hotel.getOrDefault("id", ""));
            option.setCreatedAt(LocalDateTime.now());
            
            return option;
        } catch (Exception e) {
            log.error("Error parsing hotel offer", e);
            return null;
        }
    }
    
    /**
     * Add hotel options to an itinerary
     */
    public void addHotelsToItinerary(Itinerary itinerary, List<HotelOption> hotelOptions) {
        if (hotelOptions != null) {
            hotelOptions.forEach(option -> option.setItinerary(itinerary));
            itinerary.setHotelOptions(hotelOptions);
        }
    }
}
