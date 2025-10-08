package de.unistuttgart.iste.meitrex.gamification_service.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Optional;

import de.unistuttgart.iste.meitrex.common.dapr.MockTopicPublisher;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IPlayerHexadScoreQuestionRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.PlayerHexadScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.mapper.PlayerHexadScoreMapper;
import de.unistuttgart.iste.meitrex.generated.dto.*;
import java.util.UUID;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlayerHexadScoreServiceTest {

    @Mock
    private IUserCreator userCreator;

    private final PlayerHexadScoreMapper playerHexadScoreMapper = new PlayerHexadScoreMapper();

    private PlayerHexadScoreService playerHexadScoreService;

    @Mock
    private final MockTopicPublisher mockTopicPublisher = new MockTopicPublisher();

    @Mock
    private IPlayerHexadScoreQuestionRepository  playerHexadScoreQuestionRepository;

    @Mock
    private PlayerAnswerInput input;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        playerHexadScoreService = new PlayerHexadScoreService(playerHexadScoreMapper,  userCreator, playerHexadScoreQuestionRepository, mockTopicPublisher);
    }

    @Test
    void testEvaluateWithEmptyQuestions() {
        String username = "Test name";
        // Arrange
        PlayerHexadScoreService spyService = spy(playerHexadScoreService);
        when(input.getQuestions()).thenReturn(Collections.emptyList());
        when(userCreator.fetchOrCreate(any(UUID.class)))
                .thenReturn(new UserEntity());

        float defaultValue = 100f / PlayerType.values().length;
        double expectedValue = defaultValue;

        // Act
        PlayerHexadScore result = spyService.evaluate(UUID.randomUUID(), input, username);

        verify(spyService).calculateDefault(any(), eq(true));
        verify(spyService, times(0)).calculateFromInput(input);

        verify(mockTopicPublisher).notificationEvent(any(), any(), any(), any(), any(), any());

        // Assert
        assertEquals(6, result.getScores().size(), "There should be 6 scores");
        List<PlayerTypeScore> scores = result.getScores();
        for (PlayerTypeScore score : scores) {
            assertEquals(expectedValue, score.getValue(), 0.0001, "Value for " + score.getType() + " should be " + expectedValue);
        }
    }

    @Test
    public void testEvaluateWithInput(){
        String username = "Test name";
        // Arrange
        PlayerHexadScoreService spyService = spy(playerHexadScoreService);


        AnswerInput answer1 = new AnswerInput(
                "Mock answer 1",
                Arrays.asList(PlayerType.SOCIALISER, PlayerType.PHILANTHROPIST)
        );

        AnswerInput answer2 = new AnswerInput(
                "Mock answer 2",
                Arrays.asList(PlayerType.SOCIALISER, PlayerType.PLAYER)
        );

        QuestionInput question = new QuestionInput(
                "Mock question",
                answer1,
                Arrays.asList(answer1, answer2)
        );

        PlayerAnswerInput playerAnswerInput = new PlayerAnswerInput(false, Collections.singletonList(question));
        when(input.getQuestions()).thenReturn(playerAnswerInput.getQuestions());
        when(userCreator.fetchOrCreate(any(UUID.class)))
                .thenReturn(new UserEntity());

        // Act
        PlayerHexadScore result = spyService.evaluate(UUID.randomUUID(), input, username);

        verify(spyService).calculateFromInput(input);
        verify(spyService, times(0)).calculateDefault(any(), eq(true));

        // Assert
        assertEquals(6, result.getScores().size(), "There should be 6 scores");
        Optional<PlayerTypeScore> philanthropistScore = result.getScores().stream()
                .filter(score -> score.getType() == PlayerType.PHILANTHROPIST)
                .findFirst();

        Optional<PlayerTypeScore> socialiserScore = result.getScores().stream()
                .filter(score -> score.getType() == PlayerType.SOCIALISER)
                .findFirst();

        assertEquals(100, philanthropistScore.get().getValue());
        assertEquals(50, socialiserScore.get().getValue());
    }

    @Test
    public void testEvaluateWithDefaultInputExists(){
        String username = "Test name";
        // Arrange
        PlayerHexadScoreService spyService = spy(playerHexadScoreService);


        AnswerInput answer1 = new AnswerInput(
                "Mock answer 1",
                Arrays.asList(PlayerType.SOCIALISER, PlayerType.PHILANTHROPIST)
        );

        AnswerInput answer2 = new AnswerInput(
                "Mock answer 2",
                Arrays.asList(PlayerType.SOCIALISER, PlayerType.PLAYER)
        );

        QuestionInput question = new QuestionInput(
                "Mock question",
                answer1,
                Arrays.asList(answer1, answer2)
        );

        PlayerAnswerInput playerAnswerInput = new PlayerAnswerInput(false, Collections.singletonList(question));
        when(input.getQuestions()).thenReturn(playerAnswerInput.getQuestions());

        PlayerHexadScoreEntity existingScore = new PlayerHexadScoreEntity();
        existingScore.setDefaultInput(true);
        UserEntity user = new UserEntity();
        user.setPlayerHexadScore(existingScore);
        when(userCreator.fetchOrCreate(any(UUID.class)))
                .thenReturn(user);

        // Act
        PlayerHexadScore result = spyService.evaluate(UUID.randomUUID(), input, username);

        verify(spyService).calculateFromInput(input);
        verify(spyService, times(0)).calculateDefault(any(), eq(true));

        // Assert
        assertEquals(6, result.getScores().size(), "There should be 6 scores");
        Optional<PlayerTypeScore> philanthropistScore = result.getScores().stream()
                .filter(score -> score.getType() == PlayerType.PHILANTHROPIST)
                .findFirst();

        Optional<PlayerTypeScore> socialiserScore = result.getScores().stream()
                .filter(score -> score.getType() == PlayerType.SOCIALISER)
                .findFirst();

        assertEquals(100, philanthropistScore.get().getValue());
        assertEquals(50, socialiserScore.get().getValue());
    }

    @Test
    public void testEvaluateHexadScoreAlreadyEvaluated(){
        String username = "Test name";
        // Arrange
        PlayerHexadScoreService spyService = spy(playerHexadScoreService);
        PlayerHexadScoreEntity existingScore = new PlayerHexadScoreEntity();
        existingScore.setDefaultInput(false);
        UserEntity user = new UserEntity();
        user.setPlayerHexadScore(existingScore);
        when(userCreator.fetchOrCreate(any(UUID.class)))
                .thenReturn(user);

        when(input.getQuestions()).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            spyService.evaluate(UUID.randomUUID(), input, username);
        });

        verify(spyService, times(0)).calculateDefault(any(),eq(true));
        verify(spyService, times(0)).calculateFromInput(input);
    }

    @Test
    public void testPlayerHexadScoreNotExists() {
        when(userCreator.fetchOrCreate(any(UUID.class)))
                .thenReturn(new UserEntity());
        PlayerHexadScoreService spyService = spy(playerHexadScoreService);
        assertFalse(spyService.hasHexadScore(UUID.randomUUID()));
    }

    @Test
    public void testPlayerHexadScoreExists() {
        PlayerHexadScoreService spyService = spy(playerHexadScoreService);
        PlayerHexadScoreEntity existingScore = new PlayerHexadScoreEntity();
        existingScore.setDefaultInput(false);
        UserEntity user = new UserEntity();
        user.setPlayerHexadScore(existingScore);
        when(userCreator.fetchOrCreate(any(UUID.class)))
                .thenReturn(user);
        assertTrue(spyService.hasHexadScore(UUID.randomUUID()));
    }
}
