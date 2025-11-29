package application.client;

import application.config.AmadeusConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "mock.amadeus.enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class AmadeusClient implements FlightClient {
    
    private final WebClient webClient;
    private final AmadeusConfig config;
    private String accessToken;
    
    public AmadeusClient(WebClient amadeusWebClient, AmadeusConfig config) {
        this.webClient = amadeusWebClient;
        this.config = config;
    }
    
    /**
     * Get OAuth access token from Amadeus API
     */
    private Mono<String> getAccessToken() {
        if (accessToken != null) {
            return Mono.just(accessToken);
        }
        
        Map<String, String> formData = new HashMap<>();
        formData.put("grant_type", "client_credentials");
        formData.put("client_id", config.getApiKey());
        formData.put("client_secret", config.getApiSecret());
        
        return webClient.post()
                .uri("/v1/security/oauth2/token")
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {
                    accessToken = (String) response.get("access_token");
                    return accessToken;
                })
                .doOnError(error -> {
                    log.error("Error getting Amadeus access token", error);
                    accessToken = null;
                })
                .onErrorReturn("");
    }
    
    /**
     * Search for flight offers
     */
    @Override
    public Mono<Map<String, Object>> searchFlights(String origin, String destination,
                              LocalDate departureDate, LocalDate returnDate,
                              Integer adults, Integer children) {
        return getAccessToken()
                .flatMap(token -> {
                    if (token == null || token.isEmpty()) {
                        return Mono.error(new RuntimeException("Failed to get access token"));
                    }
                    
                    String url = "/v2/shopping/flight-offers?" +
                            "originLocationCode=" + origin +
                            "&destinationLocationCode=" + destination +
                            "&departureDate=" + departureDate.format(DateTimeFormatter.ISO_LOCAL_DATE) +
                            "&adults=" + (adults != null ? adults : 1) +
                            "&children=" + (children != null ? children : 0);
                    
                    if (returnDate != null) {
                        url += "&returnDate=" + returnDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
                    }
                    
                    return webClient.get()
                            .uri(url)
                            .header("Authorization", "Bearer " + token)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                            .doOnError(error -> log.error("Error searching flights", error));
                });
    }
    
    /**
     * Get flight offer details
     */
    public Mono<Map<String, Object>> getFlightOfferDetails(String offerId) {
        return getAccessToken()
                .flatMap(token -> {
                    if (token == null || token.isEmpty()) {
                        return Mono.error(new RuntimeException("Failed to get access token"));
                    }
                    
                    return webClient.get()
                            .uri("/v2/shopping/flight-offers/" + offerId)
                            .header("Authorization", "Bearer " + token)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                            .doOnError(error -> log.error("Error getting flight offer details", error));
                });
    }
}
