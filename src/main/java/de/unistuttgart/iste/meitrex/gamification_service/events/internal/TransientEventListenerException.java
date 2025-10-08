package de.unistuttgart.iste.meitrex.gamification_service.events.internal;

/**
 * Exception indicating a recoverable error during the processing of an internal event. Throwing this exception
 * signals hat the event processing has permanently and should be retried by the surrounding infrastructure.
 *
 * @see AbstractInternalListener
 */
public class TransientEventListenerException extends InternalEventListenerException {

    public TransientEventListenerException() {
    }

    public TransientEventListenerException(String message) {
        super(message);
    }

    public TransientEventListenerException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransientEventListenerException(Throwable cause) {
        super(cause);
    }

    public TransientEventListenerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
