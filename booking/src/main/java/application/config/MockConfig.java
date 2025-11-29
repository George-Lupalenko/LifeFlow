package application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for mock clients when API keys are not available
 */
@Configuration
public class MockConfig {
    
    @Value("${mock.amadeus.enabled:false}")
    private boolean mockAmadeusEnabled;
    
    @Value("${mock.booking.enabled:false}")
    private boolean mockBookingEnabled;
    
    @Value("${mock.gemini.enabled:false}")
    private boolean mockGeminiEnabled;
    
    // The mock clients are already annotated with @ConditionalOnProperty
    // They will automatically be used when enabled
    // This config class just documents the setup
}

