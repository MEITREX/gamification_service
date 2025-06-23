package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity(name = "CompletedQuizzesGoal")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class CompletedQuizzesGoalEntity extends CountableGoalEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column
    float minimumScore;

    @Column
    OffsetDateTime trackingStartTime;

    @Column
    OffsetDateTime trackingEndTime;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    AchievementEntity achievement;

    public String generateDescription(){
        return "";
    }

    public void updateProgress(UserGoalProgressEntity userGoalProgressEntity){
        if (userGoalProgressEntity instanceof CountableUserGoalProgressEntity countableUserGoalProgressEntity){
            if (countableUserGoalProgressEntity.getCompletedCount()>= getRequiredCount()) {
                countableUserGoalProgressEntity.setCompleted(true);
            }
        }
    }

    public void updateProgress(CountableUserGoalProgressEntity userGoalProgressEntity, float score, UUID contentId){
        log.info("Updating progress for user goal progress with minimum Score {} with score {} and contentId {}",
                minimumScore, score, contentId);
        if (score >= minimumScore && !userGoalProgressEntity.getContentIds().contains(contentId)) {
            userGoalProgressEntity.setCompletedCount(userGoalProgressEntity.getCompletedCount() + 1);
            userGoalProgressEntity.getContentIds().add(contentId);
        }
        updateProgress(userGoalProgressEntity);
    }

    @Override
    public String toString() {
        return "CompletedQuizzesGoalEntity{" +
                "id=" + id +
                ", minimumScore=" + minimumScore +
                ", trackingStartTime=" + trackingStartTime +
                ", trackingEndTime=" + trackingEndTime +
                ", achievement=" + achievement.getId() +
                '}';
    }
}
