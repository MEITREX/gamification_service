package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.PlayerHexadScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.mapper.PlayerHexadScoreMapper;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.PlayerHexadScoreRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.PlayerHexadScoreService;
import de.unistuttgart.iste.meitrex.generated.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PlayerHexadScoreServiceTest {

    @Mock
    private PlayerHexadScoreRepository playerHexadScoreRepository;


    @Mock
    private PlayerHexadScoreMapper playerHexadScoreMapper;

    private PlayerHexadScoreService playerHexadScoreService;

    @Mock
    private PlayerAnswerInput input; 


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        playerHexadScoreService = new PlayerHexadScoreService(playerHexadScoreRepository, playerHexadScoreMapper);
    }

    @Test
    void testEvaluateWithEmptyQuestions() {
    // Arrange
    PlayerHexadScoreService spyService = spy(playerHexadScoreService);
    when(input.getQuestions()).thenReturn(Collections.emptyList());
    when(playerHexadScoreRepository.findByUserId(any(UUID.class)))
        .thenReturn(Optional.empty());

    float defaultValue = 100f / PlayerType.values().length;
    double expectedValue = defaultValue;

    // Act
    PlayerHexadScore result = spyService.evaluate(UUID.randomUUID(), input);

    verify(spyService).calculateDefault();
    verify(spyService, times(0)).calculateFromInput(input);
    verify(playerHexadScoreRepository, times(1)).save(any());

 
    // Assert
    assertEquals(6, result.getScores().size(), "There should be 6 scores");
    List<PlayerTypeScore> scores = result.getScores(); 
    for (PlayerTypeScore score : scores) {
        assertEquals(expectedValue, score.getValue(), 0.0001, "Value for " + score.getType() + " should be " + expectedValue);
        }
    }

    @Test
    public void testEvaluateWithInput(){
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

        PlayerAnswerInput playerAnswerInput = new PlayerAnswerInput(Collections.singletonList(question));
        when(input.getQuestions()).thenReturn(playerAnswerInput.getQuestions());
        when(playerHexadScoreRepository.findByUserId(any(UUID.class)))
        .thenReturn(Optional.empty());

        // Act
        PlayerHexadScore result = spyService.evaluate(UUID.randomUUID(), input);

        verify(spyService).calculateFromInput(input);
        verify(spyService, times(0)).calculateDefault();
        verify(playerHexadScoreRepository, times(1)).save(any());

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
    // Arrange
    PlayerHexadScoreService spyService = spy(playerHexadScoreService);
    Optional<PlayerHexadScoreEntity> existingScore = Optional.of(new PlayerHexadScoreEntity());
    when(playerHexadScoreRepository.findByUserId(any(UUID.class)))
        .thenReturn(existingScore);

    when(input.getQuestions()).thenReturn(Collections.emptyList());

    // Act & Assert
    assertThrows(IllegalStateException.class, () -> {
        spyService.evaluate(UUID.randomUUID(), input);
    });

    verify(spyService, times(0)).calculateDefault();
    verify(spyService, times(0)).calculateFromInput(input);  
    }

    @Test
    public void testPlayerHexadScoreNotExists() {
        PlayerHexadScoreService spyService = spy(playerHexadScoreService);
        assertFalse(spyService.hasHexadScore(UUID.randomUUID()));
    }

    @Test
    public void testPlayerHexadScoreExists() {
        PlayerHexadScoreService spyService = spy(playerHexadScoreService);
        Optional<PlayerHexadScoreEntity> existingScore = Optional.of(new PlayerHexadScoreEntity());
        when(playerHexadScoreRepository.findByUserId(any(UUID.class)))
                .thenReturn(existingScore);
        assertTrue(spyService.hasHexadScore(UUID.randomUUID()));
    }
}
