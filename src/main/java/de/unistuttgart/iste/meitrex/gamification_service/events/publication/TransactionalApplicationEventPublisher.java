package de.unistuttgart.iste.meitrex.gamification_service.events.publication;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * An implementation of {@link ApplicationEventPublisher} creating a new transactional context on publishing.
 *
 * @author Philipp Kunz
 */
@Component
class TransactionalApplicationEventPublisher implements ApplicationEventPublisher  {

    private ApplicationEventPublisher publisher;

    public TransactionalApplicationEventPublisher(@Autowired  ApplicationEventPublisher publisher) {
        this.publisher = Objects.requireNonNull(publisher);
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void publishEvent(ApplicationEvent event) {
        publisher.publishEvent(event);
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void publishEvent(Object event) {
        this.publisher.publishEvent(event);
    }
}
