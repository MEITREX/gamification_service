package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.time.OffsetDateTime;

@Embeddable
@Data
public class TutorImmersiveSpeechEmbeddable {
    @Column(columnDefinition = "TEXT")
    private String recentActivitiesString;
    @Column(columnDefinition = "TEXT")
    private String tutorSpeechContent;
}
