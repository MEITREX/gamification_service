package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.time.OffsetDateTime;

@Embeddable
@Data
public class TutorImmersiveSpeechEmbeddable {
    private String recentActivitiesString;
    private String tutorSpeechContent;
}
