package de.unistuttgart.iste.meitrex.gamification_service.aspects.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Loggable {

    boolean logEntry() default true;

    boolean logExit() default true;

    boolean logException() default true;

    boolean logArgs() default true;

    boolean logResult() default true;

    boolean logExecutionTime() default true;

    LogLevel inLogLevel() default LogLevel.INFO;

    LogLevel exitLogLevel() default LogLevel.DEBUG;

    LogLevel exceptionLogLevel() default LogLevel.WARN;

    enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }
}