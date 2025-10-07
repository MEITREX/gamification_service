package de.unistuttgart.iste.meitrex.gamification_service.aspects.resiliency;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retryable {

    int maxRetries() default 3;

    long backoffMillis() default 1000;

    Class<? extends Throwable>[] retryOn() default { Throwable.class };
}