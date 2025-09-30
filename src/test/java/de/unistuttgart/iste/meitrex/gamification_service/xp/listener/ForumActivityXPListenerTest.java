package de.unistuttgart.iste.meitrex.gamification_service.xp.listener;

import de.unistuttgart.iste.meitrex.common.event.ForumActivity;
import de.unistuttgart.iste.meitrex.common.event.ForumActivityEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentCourseCompletedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentForumActivityEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentCourseCompletedEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentForumActivityRepository;
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
import de.unistuttgart.iste.meitrex.gamification_service.service.reactive.xp.CourseCompletionXPListener;
import de.unistuttgart.iste.meitrex.gamification_service.service.reactive.xp.ForumActivityEventXPListener;
import de.unistuttgart.iste.meitrex.gamification_service.time.DefaultTimeService;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ForumActivityXPListenerTest {

    private record Scenario(PersistentForumActivityEvent event, ForumActivityEventXPListener listener, IUserRepository userRepository) {}

    private final ITimeService timeService = new DefaultTimeService();

    private final IUserXPAdder xpAdder = new DefaultUserXPAdder();

    private UserMapper userMapper = new UserMapper();

    private IXPLevelDistance xpLevelDistance = new DefaultXPImplementation(40, 600.0);

    private IXPLevelMapping xpLevelMapping = new DefaultXPImplementation(40, 600.0);

    @Test
    public void testNewInfoByNewUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final Scenario scenario = buildScenario(0, userID, PersistentForumActivityEvent.Type.INFO);
        scenario.listener.doProcess(scenario.event);
        assertEquals(20, scenario.userRepository.findById(userID).get().getXpValue());
    }

    @Test
    public void testNewInfoByExperiencedUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final Scenario scenario = buildScenario(500, userID, PersistentForumActivityEvent.Type.INFO);
        scenario.listener.doProcess(scenario.event);
        assertEquals(520, scenario.userRepository.findById(userID).get().getXpValue());
    }

    @Test
    public void testAcceptedAnswerByNewUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final Scenario scenario = buildScenario(0, userID, PersistentForumActivityEvent.Type.ANSWER_ACCEPTED);
        scenario.listener.doProcess(scenario.event);
        assertEquals(80, scenario.userRepository.findById(userID).get().getXpValue());
    }

    @Test
    public void testAcceptedAnswerByExperiencedUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final Scenario scenario = buildScenario(500, userID, PersistentForumActivityEvent.Type.ANSWER_ACCEPTED);
        scenario.listener.doProcess(scenario.event);
        assertEquals(580, scenario.userRepository.findById(userID).get().getXpValue());
    }

    private Scenario buildScenario(int initialXP, UUID userId, PersistentForumActivityEvent.Type type) {
        final UUID courseID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UUID forumID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UserEntity userEntity = UserEntity
                .builder()
                .id(userId)
                .xpValue(initialXP)
                .build();
        final PersistentForumActivityEvent event = PersistentForumActivityEvent
                .builder()
                .forumId(forumID)
                .type(type)
                .userId(userEntity.getId())
                .courseId(courseID)
                .build();
        final IUserRepository userRepository = mock(IUserRepository.class);
        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));
        final IPersistentForumActivityRepository eventRepository = mock(IPersistentForumActivityRepository.class);
        final IPersistentEventStatusRepository statusRepository = mock(IPersistentEventStatusRepository.class);
        final IUserCreator userCreator = new DefaultUserService(xpLevelMapping, xpLevelDistance, userRepository, userMapper, 3);
        final ForumActivityEventXPListener listener = new ForumActivityEventXPListener(
                eventRepository,
                statusRepository,
                timeService,
                xpAdder,
                userCreator
        );
        return new Scenario(event, listener, userRepository);
    }
}
