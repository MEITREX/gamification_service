package de.unistuttgart.iste.meitrex.gamification_service.events.internal;

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
