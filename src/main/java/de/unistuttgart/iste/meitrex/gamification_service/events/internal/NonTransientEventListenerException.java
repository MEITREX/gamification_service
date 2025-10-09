package de.unistuttgart.iste.meitrex.gamification_service.events.internal;

/**
 * Exception indicating a non-recoverable error during the processing of an internal event. Throwing this exception
 * signals hat the event processing has failed permanently and should not be retried by the surrounding infrastructure.
 *
 * @see AbstractInternalListener
 */
public class NonTransientEventListenerException extends InternalEventListenerException {

    public NonTransientEventListenerException() {
    }

    public NonTransientEventListenerException(String message) {
        super(message);
    }

    public NonTransientEventListenerException(String message, Throwable cause) {
        super(message, cause);
    }

    public NonTransientEventListenerException(Throwable cause) {
        super(cause);
    }

    public NonTransientEventListenerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
