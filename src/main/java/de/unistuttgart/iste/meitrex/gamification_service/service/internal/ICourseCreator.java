package de.unistuttgart.iste.meitrex.gamification_service.service.internal;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;

import java.util.UUID;

/**
 * A contract for retrieving locally stored data on a course. Retrieved data is meant for further internal processing
 * by business logic.
 *
 * @author Philipp Kunz
 */
public interface ICourseCreator {

    /**
     * Fetches an instance of {@link CourseEntity} identified by its {@link UUID} from the underlying persistence mechanism,
     * if no such instance exists, it is created, initialized, persisted and finally returned.
     *
     * @param courseId the {@link UUID} identifying the {@link CourseEntity}
     * @return an instance of {@link CourseEntity} featuring the passed courseId.
     */
    CourseEntity fetchOrCreate(UUID courseId);

}
