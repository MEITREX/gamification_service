package de.unistuttgart.iste.meitrex.gamification_service.time;

import java.time.*;

import org.springframework.stereotype.*;

@Component
class DefaultTimeService implements ITimeService {

    @Override
    public long curTime() {
        return System.currentTimeMillis();
    }

    @Override
    public LocalDate toDate() {
        return Instant.ofEpochMilli(this.curTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    @Override
    public LocalDate toDate(Long time) {
        return Instant.ofEpochMilli(time)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}

