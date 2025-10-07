package de.unistuttgart.iste.meitrex.gamification_service.aspects.resiliency;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The @Retryable annotation enables automatic retry logic for methods intercepted by the RetryAspect. When a method
 * annotated with @Retryable throws an exception, the aspect will catch it and retry the method execution according to
 * the defined parameters. The number of retries is controlled by maxRetries, the delay between retries by backoffMillis,
 * and the exception types that trigger a retry are specified in retryOn. If the thrown exception is not among the
 * retryable types, or if the maximum number of attempts is reached, the aspect logs the failure and rethrows the
 * exception. This annotation helps improve resiliency by transparently handling transient failures without complicating
 * business logic.
 *
 * @author Philipp Kunz
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retryable {

    int maxRetries() default 3;

    long backoffMillis() default 1000;

    Class<? extends Throwable>[] retryOn() default { Throwable.class };
}