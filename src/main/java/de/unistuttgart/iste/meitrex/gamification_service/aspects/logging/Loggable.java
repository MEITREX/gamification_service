package de.unistuttgart.iste.meitrex.gamification_service.aspects.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The @Loggable annotation enables automatic logging for methods intercepted by the LoggingAspect. It allows
 * fine-grained control over what gets logged, including method entry, exit, arguments, return values, exceptions,
 * and execution time. Each of these can be individually toggled, and separate log levels can be configured for entry,
 * exit, and exception messages. When applied to a method, the aspect logs messages according to these settings using
 * the specified SLF4J log levels (TRACE, DEBUG, INFO, WARN, ERROR). This provides consistent, configurable, and
 * aspect-oriented logging behavior without cluttering the business logic.
 *
 * @author Philipp Kunz
 */
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