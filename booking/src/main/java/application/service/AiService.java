package application.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class AiService {

    private final Random random = new Random();

    public String generateTravelResponse(String userQuery) {

        String city = extractCity(userQuery);
        int days = extractDays(userQuery);

        List<String> fakeFlights = generateFakeFlights(city);
        List<String> fakeHotels = generateFakeHotels(city, days);

        return buildResponse(city, days, fakeFlights, fakeHotels);
    }

    private String extractCity(String query) {
        query = query.toLowerCase();

        if (query.contains("paris")) return "Paris";
        if (query.contains("london")) return "London";
        if (query.contains("berlin")) return "Berlin";
        if (query.contains("tokyo")) return "Tokyo";
        if (query.contains("madrid")) return "Madrid";

        return "Unknown city";
    }

    private int extractDays(String query) {
        String[] words = query.split(" ");
        for (int i = 0; i < words.length; i++) {
            try {
                int val = Integer.parseInt(words[i]);
                if (i + 1 < words.length && words[i + 1].contains("day"))
                    return val;
            } catch (Exception ignored) {}
        }
        return 3; // default
    }

    private List<String> generateFakeFlights(String city) {
        return List.of(
                "✈️ Flight 1 → " + city + " | Price: $" + (100 + random.nextInt(300)),
                "✈️ Flight 2 → " + city + " | Price: $" + (150 + random.nextInt(350)),
                "✈️ Flight 3 → " + city + " | Price: $" + (200 + random.nextInt(400))
        );
    }

    private List<String> generateFakeHotels(String city, int days) {
        int base = 50 + random.nextInt(80);
        return List.of(
                "🏨 Hotel Skyline " + city + " — $" + (base * days) + " for " + days + " nights"+ "https://www.booking.com/" + city.toLowerCase() + "/hotel-skyline/d65667bc69ec1ef",
                "🏨 Central Inn " + city + " — $" + ((base + 30) * days) + "https://www.booking.com/" + city.toLowerCase() + "/central-inn/d66bc69ec1ef",
                "🏨 Luxury Suites " + city + " — $" + ((base + 60) * days)+ "https://www.booking.com/" + city.toLowerCase() + "/luxury/d66bc69ec1ef26728104"
        );
    }

    private String buildResponse(String city, int days, List<String> flights, List<String> hotels) {
        return """
            🌍 Travel options for **%s** for %s days:

            ✈️ **Flights:**
            - %s
            - %s
            - %s

            🏨 **Hotels:**
            - %s
            - %s
            - %s

            Enjoy your trip! 😊
            """.formatted(
                city,
                days,
                flights.get(0), flights.get(1), flights.get(2),
                hotels.get(0), hotels.get(1), hotels.get(2)
        );
    }
}
