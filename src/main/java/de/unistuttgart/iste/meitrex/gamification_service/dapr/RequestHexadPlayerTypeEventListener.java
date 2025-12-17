package de.unistuttgart.iste.meitrex.gamification_service.dapr;

import de.unistuttgart.iste.meitrex.common.dapr.TopicPublisher;
import de.unistuttgart.iste.meitrex.common.event.RequestHexadPlayerTypeEvent;
import de.unistuttgart.iste.meitrex.gamification_service.service.IPlayerHexadScoreService;
import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Listener for RequestHexadPlayerTypeEvent.
 * When a request is received, it fetches the user's hexad player type 
 * and publishes a UserHexadPlayerTypeSetEvent in response.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class RequestHexadPlayerTypeEventListener {

    private final IPlayerHexadScoreService playerHexadScoreService;
    private final TopicPublisher topicPublisher;

    @Topic(name = "request-hexad-player-type", pubsubName = "meitrex")
    @PostMapping(path = "/request-hexad-player-type-pubsub")
    public void onRequestHexadPlayerTypeEvent(@RequestBody CloudEvent<RequestHexadPlayerTypeEvent> cloudEvent) {
        RequestHexadPlayerTypeEvent event = cloudEvent.getData();
        
        log.info("Received RequestHexadPlayerTypeEvent for user: {}", event.getUserId());
        
        try {
            // Fetch the player hexad score for the user
            var playerHexadScore = playerHexadScoreService.getById(event.getUserId());
            
            if (playerHexadScore != null) {
                log.info("Publishing UserHexadPlayerTypeSetEvent for user: {}", event.getUserId());
                playerHexadScoreService.sendUserHexadPlayerTypeSetEvent(event.getUserId(), playerHexadScore);
            } else {
                log.warn("No hexad player type found for user: {}", event.getUserId());
            }
        } catch (Exception e) {
            log.error("Error handling RequestHexadPlayerTypeEvent for user {}: {}", 
                    event.getUserId(), e.getMessage(), e);
        }
    }
}
