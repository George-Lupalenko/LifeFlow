package application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HotelApiConfig {
    
    @Value("${booking.api.key:}")
    private String apiKey;
    
    @Value("${booking.api.base-url}")
    private String baseUrl;
    
    @Bean
    public WebClient bookingComWebClient() {
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        
        if (apiKey != null && !apiKey.isEmpty()) {
            builder.defaultHeader("X-RapidAPI-Key", apiKey);
        }
        
        return builder.build();
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
}
