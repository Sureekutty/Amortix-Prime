package com.amortix.prime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * AMORTIX | PRIME
 * Cooperative Credit & Core Ledger Architecture
 *
 * Monolithic Server-Side Rendered Architecture (Zero JavaScript on Frontend).
 * Java 21 LTS | Spring Boot 3 | Spring Data MongoDB | Spring Security 6
 */
@SpringBootApplication
@EnableMongoRepositories
public class AmortixPrimeApplication {

    public static void main(String[] args) {
        SpringApplication.run(AmortixPrimeApplication.class, args);
    }
}
