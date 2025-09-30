package de.unistuttgart.iste.meitrex.gamification_service.xp.listener;

import de.unistuttgart.iste.meitrex.gamification_service.dapr.CourseCompletedEventListener;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentChapterCompletedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentCourseCompletedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentChapterCompletedEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentCourseCompletedEventRepository;
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
import de.unistuttgart.iste.meitrex.gamification_service.service.reactive.xp.ChapterCompletionXPListener;
import de.unistuttgart.iste.meitrex.gamification_service.service.reactive.xp.CourseCompletionXPListener;
import de.unistuttgart.iste.meitrex.gamification_service.time.DefaultTimeService;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CourseCompletionXPListenerTest {

    private record Scenario(PersistentCourseCompletedEvent event, CourseCompletionXPListener listener, IUserRepository userRepository) {}

    private final ITimeService timeService = new DefaultTimeService();

    private final IUserXPAdder xpAdder = new DefaultUserXPAdder();

    private UserMapper userMapper = new UserMapper();

    private IXPLevelDistance xpLevelDistance = new DefaultXPImplementation(40, 600.0);

    private IXPLevelMapping xpLevelMapping = new DefaultXPImplementation(40, 600.0);

    @Test
    public void testCourseCompletionByNewUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final Scenario scenario = buildScenario(0, userID);
        scenario.listener.doProcess(scenario.event);
        assertEquals(500, scenario.userRepository.findById(userID).get().getXpValue());
    }

    @Test
    public void testCourseCompletionByExperiencedUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final Scenario scenario = buildScenario(200, userID);
        scenario.listener.doProcess(scenario.event);
        assertEquals(700, scenario.userRepository.findById(userID).get().getXpValue());
    }

    private Scenario buildScenario(int initialXP, UUID userId) {
        final UUID courseID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UserEntity userEntity = UserEntity
                .builder()
                .id(userId)
                .xpValue(initialXP)
                .build();
        final PersistentCourseCompletedEvent event = PersistentCourseCompletedEvent
                .builder()
                .userId(userEntity.getId())
                .courseId(courseID)
                .build();
        final IUserRepository userRepository = mock(IUserRepository.class);
        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));
        final IPersistentCourseCompletedEventRepository eventRepository = mock(IPersistentCourseCompletedEventRepository.class);
        final IPersistentEventStatusRepository statusRepository = mock(IPersistentEventStatusRepository.class);
        final IUserCreator userCreator = new DefaultUserService(xpLevelMapping, xpLevelDistance, userRepository, userMapper, 3);
        final CourseCompletionXPListener courseCompletionXPListener = new CourseCompletionXPListener(
                eventRepository,
                statusRepository,
                timeService,
                userCreator,
                xpAdder
        );
        return new Scenario(event, courseCompletionXPListener, userRepository);
    }
}
