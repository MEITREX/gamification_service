package de.unistuttgart.iste.meitrex.gamification_service.time;

import java.time.*;

import de.unistuttgart.iste.meitrex.gamification_service.aspects.logging.Loggable;
import org.springframework.stereotype.*;

@Component
public class DefaultTimeService implements ITimeService {

    @Override
    @Loggable(
            inLogLevel = Loggable.LogLevel.DEBUG,
            exitLogLevel = Loggable.LogLevel.DEBUG,
            exceptionLogLevel = Loggable.LogLevel.WARN,
            logExecutionTime = false
    )
    public long curTime() {
        return System.currentTimeMillis();
    }

    @Override
    @Loggable(
            inLogLevel = Loggable.LogLevel.DEBUG,
            exitLogLevel = Loggable.LogLevel.DEBUG,
            exceptionLogLevel = Loggable.LogLevel.WARN,
            logExecutionTime = false
    )
    public LocalDate toDate() {
        return Instant.ofEpochMilli(this.curTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    @Override
    @Loggable(
            inLogLevel = Loggable.LogLevel.DEBUG,
            exitLogLevel = Loggable.LogLevel.DEBUG,
            exceptionLogLevel = Loggable.LogLevel.WARN,
            logExecutionTime = false
    )
    public LocalDate toDate(Long time) {
        return Instant.ofEpochMilli(time)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}

