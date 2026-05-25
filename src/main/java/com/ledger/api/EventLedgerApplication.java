package com.ledger.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.ledger.api.*")
public class EventLedgerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventLedgerApplication.class, args);
    }
}
