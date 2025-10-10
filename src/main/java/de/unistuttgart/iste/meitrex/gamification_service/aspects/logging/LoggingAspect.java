package de.unistuttgart.iste.meitrex.gamification_service.aspects.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Aspect
@Component
public class LoggingAspect {

    private static final String ERR_MSG_ENTERING_WITH_ARGS = "Entering {} with args {}";

    private static final String ERR_MSG_ENTERING = "Entering {}";

    private static final String ERR_MSG_EXITING_WITH_RESULT = "Exiting {} with result {}";

    private static final String ERR_MSG_EXITING = "Exiting {}";

    private static final String ERR_MSG_EXECUTION_TIME = "Execution time for {}: {} ms";

    private static final String ERR_MSG_EXCEPTION = "Exception in {}: {}";

    @Around("@annotation(loggable)")
    public Object around(ProceedingJoinPoint pjp, Loggable loggable) throws Throwable {
        final Logger logger = LoggerFactory.getLogger(pjp.getTarget().getClass());
        final MethodSignature signature = (MethodSignature) pjp.getSignature();
        final String methodName = signature.toShortString();
        final long start = System.currentTimeMillis();
        try {
            logEntry(pjp, loggable, logger, methodName);
            Object result = pjp.proceed();
            logExit(result, loggable, logger, methodName);
            logExecutionTime(start, loggable, logger, methodName);
            return result;
        } catch (Throwable t) {
            logException(t, loggable, logger, methodName);
            throw t;
        }
    }

    private void logEntry(ProceedingJoinPoint pjp, Loggable loggable, Logger logger, String methodName) {
        if (loggable.logEntry()) {
            if (loggable.logArgs()) {
                log(logger, loggable.inLogLevel(), ERR_MSG_ENTERING_WITH_ARGS, methodName, Arrays.toString(pjp.getArgs()));
            } else {
                log(logger, loggable.inLogLevel(), ERR_MSG_ENTERING, methodName);
            }
        }
    }

    private void logExit(Object result, Loggable loggable, Logger logger, String methodName) {
        if (loggable.logExit()) {
            if (loggable.logResult()) {
                log(logger, loggable.exitLogLevel(), ERR_MSG_EXITING_WITH_RESULT, methodName, result);
            } else {
                log(logger, loggable.exitLogLevel(), ERR_MSG_EXITING, methodName);
            }
        }
    }

    private void logExecutionTime(long start, Loggable loggable, Logger logger, String methodName) {
        if (loggable.logExecutionTime()) {
            long duration = System.currentTimeMillis() - start;
            log(logger, loggable.exitLogLevel(), ERR_MSG_EXECUTION_TIME, methodName, duration);
        }
    }

    private void logException(Throwable t, Loggable loggable, Logger logger, String methodName) {
        if (loggable.logException()) {
            log(logger, loggable.exceptionLogLevel(), ERR_MSG_EXCEPTION, methodName, t.toString());
        }
    }

    private void log(Logger logger, Loggable.LogLevel level, String msg, Object... args) {
        switch (level) {
            case TRACE -> { if (logger.isTraceEnabled()) logger.trace(msg, args); }
            case DEBUG -> { if (logger.isDebugEnabled()) logger.debug(msg, args); }
            case INFO  -> { if (logger.isInfoEnabled())  logger.info(msg, args); }
            case WARN  -> { if (logger.isWarnEnabled())  logger.warn(msg, args); }
            case ERROR -> { if (logger.isErrorEnabled()) logger.error(msg, args); }
        }
    }
}
