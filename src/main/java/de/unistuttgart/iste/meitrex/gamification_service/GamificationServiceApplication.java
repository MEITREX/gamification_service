package de.unistuttgart.iste.meitrex.gamification_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unistuttgart.iste.meitrex.common.dapr.TopicPublisher;
import de.unistuttgart.iste.meitrex.common.event.UserProgressUpdatedEvent;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * This is the entry point of the application.
 * <p>
 * TODO: Rename the package and the class to match the microservice name.
 */
@SpringBootApplication
@Slf4j
@EnableScheduling
public class GamificationServiceApplication {

    public static void main(String[] args) {
        Arrays.stream(args).map(arg -> "Received argument: " + arg).forEach(log::info);
        SpringApplication.run(GamificationServiceApplication.class, args);
    }



}
