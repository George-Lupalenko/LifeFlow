package application;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookingApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .directory("D:/Java/LifeFlow/booking") // путь к папке с .env
                .load(); // загрузка .env
        System.setProperty("OPENAI_API_KEY", dotenv.get("OPENAI_API_KEY"));
        System.setProperty("AMADEUS_API_KEY", dotenv.get("AMADEUS_API_KEY"));
        System.setProperty("BOOKING_API_KEY", dotenv.get("BOOKING_API_KEY"));// передача в Spring

        SpringApplication.run(BookingApplication.class, args);
    }
}
