package de.unistuttgart.iste.meitrex.gamification_service.controller;

import de.unistuttgart.iste.meitrex.common.event.ContentProgressedEvent;
import de.unistuttgart.iste.meitrex.common.event.ForumActivityEvent;
import de.unistuttgart.iste.meitrex.common.event.UserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.service.AchievementService;
import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SubscriptionController {
    private final AchievementService achievementService;

    /**
     * Listens to the content-progressed topic and processes the user progress.
     */
    @Topic(name = "content-progressed", pubsubName = "meitrex")
    @PostMapping(path = "/content-progressed-pubsub")
    public Mono<Void> logUserProgress(@RequestBody final CloudEvent<ContentProgressedEvent> cloudEvent) {
        return Mono.fromRunnable(() -> {
            log.info("Received content-progressed event: {}", cloudEvent.getData());
            achievementService.progressUserProgress(cloudEvent.getData());
        })
        .subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Dapr topic subscription to recalculate the skill levels of a user for a specific chapter when the user
     * completes an assessment in that chapter.
     */
    @Topic(name = "user-progress-updated", pubsubName = "meitrex")
    @PostMapping(path = "/user-progress-pubsub")
    public Mono<Void> onUserProgress(@RequestBody final CloudEvent<UserProgressUpdatedEvent> cloudEvent) {
        return Mono.fromRunnable(() -> {
            log.info("Received user-progress event: {}", cloudEvent.getData());
            achievementService.chapterProgress(cloudEvent.getData());
        })
        .subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Dapr topic subscription to get the forum activity of a user.
     */
    @Topic(name = "forum-activity", pubsubName = "meitrex")
    @PostMapping(path = "/forum-activity-pubsub")
    public Mono<Void> onForumActivity(@RequestBody final CloudEvent<ForumActivityEvent> cloudEvent) {
        return Mono.fromRunnable(() -> {
            try {
                log.info("Received forum-activity event: {}", cloudEvent.getData());
                achievementService.forumProgress(cloudEvent.getData());
            } catch (Exception e) {
                // we need to catch all exceptions because otherwise if some invalid data is in the message queue
                // it will never get processed and instead the service will just crash forever
                log.error("Error while processing user progress event", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic()).then();
    }
}
