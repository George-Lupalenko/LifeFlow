package application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration for OpenAI API
 * Note: This WebClient is created but OpenAiClient creates its own with auth headers
 */
@Configuration
public class OpenAiConfig {
    
    @Value("${openai.api.key}")
    private String apiKey;
    
    @Value("${openai.model:gpt-4o-mini}")
    private String model;
    
    private static final String OPENAI_API_BASE_URL = "https://api.openai.com/v1";
    
    @Bean
    public WebClient geminiWebClient() {
        // Keep this bean name for compatibility, but it's actually for OpenAI now
        return WebClient.builder()
                .baseUrl(OPENAI_API_BASE_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public String getModel() {
        return model;
    }
    
    public String getBaseUrl() {
        return OPENAI_API_BASE_URL;
    }
}
