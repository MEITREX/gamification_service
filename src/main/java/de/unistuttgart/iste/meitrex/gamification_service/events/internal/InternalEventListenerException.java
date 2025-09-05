package de.unistuttgart.iste.meitrex.gamification_service.events.internal;

abstract class InternalEventListenerException extends RuntimeException  {

    public InternalEventListenerException() {
    }

    public InternalEventListenerException(String message) {
        super(message);
    }

    public InternalEventListenerException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalEventListenerException(Throwable cause) {
        super(cause);
    }

    public InternalEventListenerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
