package com.lifeflow.bank;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BankStatementAnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankStatementAnalyticsApplication.class, args);
    }
}