package com.shieldnet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShieldNetApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShieldNetApplication.class, args);
    }
}
