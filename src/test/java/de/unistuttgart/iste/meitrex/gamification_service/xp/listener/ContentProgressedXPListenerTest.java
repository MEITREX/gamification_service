package de.unistuttgart.iste.meitrex.gamification_service.xp.listener;

import de.unistuttgart.iste.meitrex.common.event.ContentProgressedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentContentProgressedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentContentProgressedRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.mapper.UserMapper;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.DefaultUserService;
import de.unistuttgart.iste.meitrex.gamification_service.service.functional.DefaultXPImplementation;
import de.unistuttgart.iste.meitrex.gamification_service.service.functional.IXPLevelDistance;
import de.unistuttgart.iste.meitrex.gamification_service.service.functional.IXPLevelMapping;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.DefaultUserXPAdder;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserXPAdder;
import de.unistuttgart.iste.meitrex.gamification_service.service.reactive.xp.ContentProgressedXPListener;
import de.unistuttgart.iste.meitrex.gamification_service.time.DefaultTimeService;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentProgressedXPListenerTest {


    private record Scenario(PersistentContentProgressedEvent event, ContentProgressedXPListener listener, IUserRepository userRepository) {}


    private final ITimeService timeService = new DefaultTimeService();

    private final IUserXPAdder xpAdder = new DefaultUserXPAdder();

    private UserMapper userMapper = new UserMapper();

    private IXPLevelDistance xpLevelDistance = new DefaultXPImplementation(40, 600.0);

    private IXPLevelMapping xpLevelMapping = new DefaultXPImplementation(40, 600.0);

    // Assignments

    @Test
    public void testAssignmentCompletionByNewUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        Scenario scenario = buildScenario(userID, 0, ContentProgressedEvent.ContentType.ASSIGNMENT, 1.0, 0);
        scenario.listener.doProcess(scenario.event);
        assertEquals(80, scenario.userRepository.findById(userID).get().getXpValue());
    }

    @Test
    public void testAssignmentCompletionByExperiencedUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        Scenario scenario = buildScenario(userID, 40, ContentProgressedEvent.ContentType.ASSIGNMENT, 1.0, 0);
        scenario.listener.doProcess(scenario.event);
        assertEquals(120, scenario.userRepository.findById(userID).get().getXpValue());
    }

    //Quizzes

    @Test
    public void testQuiz10CompletionByNewUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        Scenario scenario = buildScenario(userID, 0, ContentProgressedEvent.ContentType.QUIZ, 1.0, 10);
        scenario.listener.doProcess(scenario.event);
        assertEquals(20, scenario.userRepository.findById(userID).get().getXpValue());
    }

    @Test
    public void testQuiz10CompletionByExperiencedUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        Scenario scenario = buildScenario(userID, 40, ContentProgressedEvent.ContentType.QUIZ, 1.0, 10);
        scenario.listener.doProcess(scenario.event);
        assertEquals(60, scenario.userRepository.findById(userID).get().getXpValue());
    }

    @Test
    public void testQuiz5CompletionByNewUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        Scenario scenario = buildScenario(userID, 0, ContentProgressedEvent.ContentType.QUIZ, 1.0, 5);
        scenario.listener.doProcess(scenario.event);
        assertEquals(10, scenario.userRepository.findById(userID).get().getXpValue());
    }

    @Test
    public void testQuiz5CompletionByExperiencedUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        Scenario scenario = buildScenario(userID, 40, ContentProgressedEvent.ContentType.QUIZ, 1.0, 5);
        scenario.listener.doProcess(scenario.event);
        assertEquals(50, scenario.userRepository.findById(userID).get().getXpValue());
    }

    // Unsuccessful Quizzes

    @Test
    public void testUnsuccessfulQuiz10PartialCompletionByNewUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        Scenario scenario = buildScenario(userID, 0, ContentProgressedEvent.ContentType.QUIZ, .5, 10, false);
        scenario.listener.doProcess(scenario.event);
        assertEquals(0, scenario.userRepository.findById(userID).get().getXpValue());
    }

    @Test
    public void testUnsuccessfulQuiz5PartialCompletionByNewUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        Scenario scenario = buildScenario(userID, 0, ContentProgressedEvent.ContentType.QUIZ, .5, 5, false);
        scenario.listener.doProcess(scenario.event);
        assertEquals(0, scenario.userRepository.findById(userID).get().getXpValue());
    }

    //Flashcards

    @Test
    public void testFlashcards10CompletionByNewUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        Scenario scenario = buildScenario(userID, 0, ContentProgressedEvent.ContentType.FLASHCARDS, 1.0, 10);
        scenario.listener.doProcess(scenario.event);
        assertEquals(20, scenario.userRepository.findById(userID).get().getXpValue());
    }

    @Test
    public void testQFlashcards10CompletionByExperiencedUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        Scenario scenario = buildScenario(userID, 40, ContentProgressedEvent.ContentType.FLASHCARDS, 1.0, 10);
        scenario.listener.doProcess(scenario.event);
        assertEquals(60, scenario.userRepository.findById(userID).get().getXpValue());
    }

    @Test
    public void testFlashcards5CompletionByNewUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        Scenario scenario = buildScenario(userID, 0, ContentProgressedEvent.ContentType.FLASHCARDS, 1.0, 5);
        scenario.listener.doProcess(scenario.event);
        assertEquals(10, scenario.userRepository.findById(userID).get().getXpValue());
    }

    @Test
    public void testFlashcards5CompletionByExperiencedUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        Scenario scenario = buildScenario(userID, 40, ContentProgressedEvent.ContentType.FLASHCARDS, 1.0, 5);
        scenario.listener.doProcess(scenario.event);
        assertEquals(50, scenario.userRepository.findById(userID).get().getXpValue());
    }

    // Unsuccessful Flashcards

    @Test
    public void testUnsuccessfulFlashcards10PartialCompletionByNewUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        Scenario scenario = buildScenario(userID, 0, ContentProgressedEvent.ContentType.FLASHCARDS, .5, 10, false);
        scenario.listener.doProcess(scenario.event);
        assertEquals(0, scenario.userRepository.findById(userID).get().getXpValue());
    }

    @Test
    public void testUnsuccessfulFlashcards5PartialCompletionByNewUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        Scenario scenario = buildScenario(userID, 0, ContentProgressedEvent.ContentType.FLASHCARDS, .5, 5, false);
        scenario.listener.doProcess(scenario.event);
        assertEquals(0, scenario.userRepository.findById(userID).get().getXpValue());
    }

    private Scenario buildScenario(UUID userID, int initialXP, ContentProgressedEvent.ContentType type, double correctness, int answerCount) {
        return buildScenario(userID, initialXP, type, correctness, answerCount, true);
    }


    private Scenario buildScenario(UUID userID, int initialXP, ContentProgressedEvent.ContentType type, double correctness, int answerCount, boolean success) {
        final UUID contentId =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UserEntity userEntity = UserEntity
                .builder()
                .id(userID)
                .xpValue(initialXP)
                .build();
        final PersistentContentProgressedEvent persistentContentProgressedEvent = PersistentContentProgressedEvent
                .builder()
                .contentId(contentId)
                .userId(userEntity.getId())
                .success(success)
                .correctness(correctness)
                .hintsUsed(10)
                .timeToComplete(100)
                .contentType(type)
                .responses(IntStream.range(0, answerCount).mapToObj(count -> new PersistentContentProgressedEvent.PersistentResponse()).toList())
                .build();
        final IUserRepository userRepository = mock(IUserRepository.class);
        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));
        final IPersistentContentProgressedRepository eventRepository = mock(IPersistentContentProgressedRepository.class);
        final IPersistentEventStatusRepository statusRepository = mock(IPersistentEventStatusRepository.class);
        final IUserCreator userCreator = new DefaultUserService(xpLevelMapping, xpLevelDistance, userRepository, userMapper, 3);
        final ContentProgressedXPListener listener = new ContentProgressedXPListener(
                eventRepository,
                statusRepository,
                timeService,
                userCreator,
                xpAdder
        );
        return new Scenario(persistentContentProgressedEvent, listener, userRepository);
    }
}
