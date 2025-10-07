package de.unistuttgart.iste.meitrex.gamification_service.aspects.resiliency;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RetryAspect {

    private static boolean isRetryable(Throwable t, Class<? extends Throwable>[] retryOn) {
        for (Class<? extends Throwable> clazz : retryOn) {
            if (clazz.isAssignableFrom(t.getClass())) {
                return true;
            }
        }
        return false;
    }

    private static final String ERR_MSG_FINAL_FAILURE = "The final attempt {} failed for {} due to an exception.";

    private static final String ERR_MSG_RETRYABLE = "The attempt {} failed for {} due to an exception. Retrying.";

    private static final String ERR_MSG_NOT_RETRYABLE = "The attempt {} failed for {} due to an exception. Aborting.";

    private static final Logger logger = LoggerFactory.getLogger(RetryAspect.class);

    @Around("@annotation(retryable)")
    public Object around(ProceedingJoinPoint pjp, Retryable retryable)
            throws Throwable {
        Object returnValue = null;
        final int maxRetries = retryable.maxRetries();
        final long initialBackoff = retryable.backoffMillis();
        final Class<? extends Throwable>[] retryOn = retryable.retryOn();
        int curAttempt = 1;
        while (curAttempt <= maxRetries) {
            try {
                returnValue = pjp.proceed();
                break;
            } catch (Throwable t) {
                if (curAttempt == maxRetries) {
                    logger.warn(ERR_MSG_FINAL_FAILURE, curAttempt, pjp.getSignature(), t);
                    throw t;
                }
                if(!isRetryable(t, retryOn)) {
                    logger.warn(ERR_MSG_NOT_RETRYABLE, curAttempt, pjp.getSignature(), t);
                    throw t;
                }
                logger.warn(ERR_MSG_RETRYABLE, curAttempt, pjp.getSignature(), t);
                Thread.sleep(initialBackoff * curAttempt);
                curAttempt++;
            }
        }
        return returnValue;
    }
}