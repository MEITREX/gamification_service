package de.unistuttgart.iste.meitrex.gamification_service.service.reactive.xp;


import java.util.Objects;

import org.springframework.stereotype.*;
import org.springframework.context.event.*;
import org.springframework.beans.factory.annotation.*;

import de.unistuttgart.iste.meitrex.gamification_service.service.internal.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.domain.*;

@Component
public class AchievementCompletedEventListener {

    private final IUserXPAdder userXPAdder;

    public AchievementCompletedEventListener(@Autowired IUserXPAdder userXPAdder) {
        this.userXPAdder = Objects.requireNonNull(userXPAdder);
    }

    @EventListener
    public void onAchievementCompleted(AchievementCompletedEvent event) {
        userXPAdder.add(event.getAchiever(), IUserXPAdder.Cause.ACHIEVEMENT_COMPLETED);
    }
}
