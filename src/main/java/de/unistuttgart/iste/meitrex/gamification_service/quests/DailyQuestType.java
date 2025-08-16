package de.unistuttgart.iste.meitrex.gamification_service.quests;

public enum DailyQuestType {
    EXERCISE,       // work on assessments not yet completed
    SKILL_LEVEL,    // work on assessments with low skill level
    LEARNING,       // learn new content (slides/videos)
    SPECIALTY       // unique quests depending on user's recommendation score ("player type")
}