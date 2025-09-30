package de.unistuttgart.iste.meitrex.gamification_service.xp.listener;

import de.unistuttgart.iste.meitrex.course_service.persistence.entity.ChapterEntity;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentChapterCompletedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentChapterCompletedEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
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
import de.unistuttgart.iste.meitrex.gamification_service.time.DefaultTimeService;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import de.unistuttgart.iste.meitrex.generated.dto.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChapterCompletionXPListenerTest {

    private record Scenario(PersistentChapterCompletedEvent event, ChapterCompletionXPListener listener, IUserRepository userRepository) {}


    private final ITimeService timeService = new DefaultTimeService();

    private final IUserXPAdder xpAdder = new DefaultUserXPAdder();

    private UserMapper userMapper = new UserMapper();

    private IXPLevelDistance xpLevelDistance = new DefaultXPImplementation(40, 600.0);

    private IXPLevelMapping xpLevelMapping = new DefaultXPImplementation(40, 600.0);


    @Test
    public void testChapterCompletionByNewUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final Scenario scenario = buildScenario(0, userID);
        scenario.listener.doProcess(scenario.event);
        assertEquals(200, scenario.userRepository.findById(userID).get().getXpValue());
    }

    @Test
    public void testChapterCompletionByExperiencedUser() {
        final UUID userID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final Scenario scenario = buildScenario(200, userID);
        scenario.listener.doProcess(scenario.event);
        assertEquals(400, scenario.userRepository.findById(userID).get().getXpValue());

    }

    private Scenario buildScenario(int initialXP, UUID userId) {
        final UUID courseID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UUID chapterID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UUID eventID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UserEntity userEntity = UserEntity
                .builder()
                .id(userId)
                .xpValue(initialXP)
                .build();
        final PersistentChapterCompletedEvent persistentChapterCompletedEvent = PersistentChapterCompletedEvent
                .builder()
                .uuid(userId)
                .uuid(eventID)
                .userId(userEntity.getId())
                .courseId(courseID)
                .chapterId(chapterID)
                .build();
        final IUserRepository userRepository = mock(IUserRepository.class);
        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));
        final IPersistentChapterCompletedEventRepository eventRepository = mock(IPersistentChapterCompletedEventRepository.class);
        final IPersistentEventStatusRepository statusRepository = mock(IPersistentEventStatusRepository.class);
        final IUserCreator userCreator = new DefaultUserService(xpLevelMapping, xpLevelDistance, userRepository, userMapper, 3);
        final ChapterCompletionXPListener chapterCompletionXPListener = new ChapterCompletionXPListener(
                eventRepository,
                statusRepository,
                timeService,
                userCreator,
                xpAdder
        );
        return new Scenario(persistentChapterCompletedEvent, chapterCompletionXPListener, userRepository);
    }

}
