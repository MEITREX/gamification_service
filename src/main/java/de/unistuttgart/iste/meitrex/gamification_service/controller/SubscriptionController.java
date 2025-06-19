package de.unistuttgart.iste.meitrex.gamification_service.controller;

import de.unistuttgart.iste.meitrex.common.event.ContentProgressedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.service.AchievementService;
import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SubscriptionController {
    private final AchievementService achievementService;

    /**
     * Listens to the content-progressed topic and logs the user progress.
     */
    @Topic(name = "content-progressed", pubsubName = "meitrex")
    @PostMapping(path = "/content-progressed-pubsub")
    public Mono<Void> logUserProgress(@RequestBody final CloudEvent<ContentProgressedEvent> cloudEvent) {
        return Mono.fromRunnable(() -> {
            try {
                log.info("Received content-progressed event: {}", cloudEvent.getData());
                achievementService.logUserProgress(cloudEvent.getData());
            } catch (final Exception e) {
                log.error("Error while processing logUserProgress event. {}", e.getMessage());
            }
        });
    }



}
