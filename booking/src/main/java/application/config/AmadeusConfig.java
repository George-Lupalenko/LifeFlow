package application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AmadeusConfig {
    
    @Value("${amadeus.api.key}")
    private String apiKey;
    
    @Value("${amadeus.api.secret}")
    private String apiSecret;
    
    @Value("${amadeus.api.base-url}")
    private String baseUrl;
    
    @Bean
    public WebClient amadeusWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public String getApiSecret() {
        return apiSecret;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
}
