package de.unistuttgart.iste.meitrex.gamification_service.service.internal;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.*;

public interface ICourseMembershipHandler {

    UserCourseDataEntity addUserToCourseIfNotAlready(CourseEntity course, UserEntity user);

}
