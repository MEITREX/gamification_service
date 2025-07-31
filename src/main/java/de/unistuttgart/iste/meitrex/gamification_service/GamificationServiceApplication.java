package de.unistuttgart.iste.meitrex.gamification_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import java.util.Arrays;

/**
 * This is the entry point of the application.
 * <p>
 * TODO: Rename the package and the class to match the microservice name.
 */
@SpringBootApplication
@Slf4j
public class GamificationServiceApplication {

    public static void main(String[] args) {
        Arrays.stream(args).map(arg -> "Received argument: " + arg).forEach(log::info);
        SpringApplication.run(GamificationServiceApplication.class, args);
    }

}
