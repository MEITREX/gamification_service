package de.unistuttgart.iste.meitrex.gamification_service.dapr;

import de.unistuttgart.iste.meitrex.common.dapr.TopicPublisher;
import de.unistuttgart.iste.meitrex.common.event.RequestHexadPlayerTypeEvent;
import de.unistuttgart.iste.meitrex.gamification_service.service.IPlayerHexadScoreService;
import de.unistuttgart.iste.meitrex.generated.dto.PlayerHexadScore;
import de.unistuttgart.iste.meitrex.generated.dto.PlayerType;
import de.unistuttgart.iste.meitrex.generated.dto.PlayerTypeScore;
import io.dapr.client.domain.CloudEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Unit tests for RequestHexadPlayerTypeEventListener.
 */
@ExtendWith(MockitoExtension.class)
class RequestHexadPlayerTypeEventListenerTest {

    @Mock
    private IPlayerHexadScoreService playerHexadScoreService;

    @Mock
    private TopicPublisher topicPublisher;

    @InjectMocks
    private RequestHexadPlayerTypeEventListener eventListener;

    private UUID userId;
    private CloudEvent<RequestHexadPlayerTypeEvent> cloudEvent;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        RequestHexadPlayerTypeEvent event = RequestHexadPlayerTypeEvent.builder()
                .userId(userId)
                .build();
        cloudEvent = mock(CloudEvent.class);
        when(cloudEvent.getData()).thenReturn(event);
    }

    /**
     * Test handling of RequestHexadPlayerTypeEvent with existing player type.
     */
    @Test
    void testOnRequestHexadPlayerTypeEvent_WithExistingPlayerType() {
        PlayerHexadScore playerHexadScore = createMockPlayerHexadScore();
        when(playerHexadScoreService.getById(userId)).thenReturn(playerHexadScore);

        eventListener.onRequestHexadPlayerTypeEvent(cloudEvent);

        verify(playerHexadScoreService, times(1)).getById(userId);
        verify(playerHexadScoreService, times(1))
                .sendUserHexadPlayerTypeSetEvent(userId, playerHexadScore);
    }

    /**
     * Test handling of RequestHexadPlayerTypeEvent with null player type.
     */
    @Test
    void testOnRequestHexadPlayerTypeEvent_WithNullPlayerType() {
        when(playerHexadScoreService.getById(userId)).thenReturn(null);

        eventListener.onRequestHexadPlayerTypeEvent(cloudEvent);

        verify(playerHexadScoreService, times(1)).getById(userId);
        verify(playerHexadScoreService, never())
                .sendUserHexadPlayerTypeSetEvent(any(), any());
    }

    /**
     * Test handling of RequestHexadPlayerTypeEvent when an exception occurs.
     */
    @Test
    void testOnRequestHexadPlayerTypeEvent_WithException() {
        when(playerHexadScoreService.getById(userId))
                .thenThrow(new RuntimeException("Database error"));

        eventListener.onRequestHexadPlayerTypeEvent(cloudEvent);

        verify(playerHexadScoreService, times(1)).getById(userId);
        verify(playerHexadScoreService, never())
                .sendUserHexadPlayerTypeSetEvent(any(), any());
    }

    /**
     * Test handling of RequestHexadPlayerTypeEvent when sending event throws an exception.
     */
    @Test
    void testOnRequestHexadPlayerTypeEvent_SendEventThrowsException() {
        PlayerHexadScore playerHexadScore = createMockPlayerHexadScore();
        when(playerHexadScoreService.getById(userId)).thenReturn(playerHexadScore);
        doThrow(new RuntimeException("Event publishing error"))
                .when(playerHexadScoreService)
                .sendUserHexadPlayerTypeSetEvent(userId, playerHexadScore);

        eventListener.onRequestHexadPlayerTypeEvent(cloudEvent);

        verify(playerHexadScoreService, times(1)).getById(userId);
        verify(playerHexadScoreService, times(1))
                .sendUserHexadPlayerTypeSetEvent(userId, playerHexadScore);
    }

    /**
     * Creates a mock PlayerHexadScore for testing. Values are arbitrary and not significant.
     */
    private PlayerHexadScore createMockPlayerHexadScore() {
        List<PlayerTypeScore> scores = Arrays.asList(
                new PlayerTypeScore(PlayerType.ACHIEVER, 0.85),
                new PlayerTypeScore(PlayerType.PLAYER, 0.70),
                new PlayerTypeScore(PlayerType.SOCIALISER, 0.65),
                new PlayerTypeScore(PlayerType.FREE_SPIRIT, 0.60),
                new PlayerTypeScore(PlayerType.PHILANTHROPIST, 0.55),
                new PlayerTypeScore(PlayerType.DISRUPTOR, 0.50)
        );
        return new PlayerHexadScore(false, scores);
    }
}
