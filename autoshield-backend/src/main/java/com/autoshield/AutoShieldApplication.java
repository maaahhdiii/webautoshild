package com.autoshield;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for AutoShield Backend API
 * Provides security monitoring, metrics collection, and firewall management
 */
@SpringBootApplication
@EnableScheduling
public class AutoShieldApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoShieldApplication.class, args);
    }
}
