package de.unistuttgart.iste.meitrex.gamification_service.time;

import org.springframework.stereotype.*;

@Component
class DefaultTimeService implements ITimeService {

    @Override
    public long now() {
        return System.currentTimeMillis();
    }

}

