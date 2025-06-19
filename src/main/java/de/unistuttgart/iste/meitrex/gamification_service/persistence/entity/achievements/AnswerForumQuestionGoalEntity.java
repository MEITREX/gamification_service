package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "AnswerForumQuestionGoal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnswerForumQuestionGoalEntity extends CountableGoalEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    public String generateDescription(){
        return "";
    }

    public void updateProgress(UserGoalProgressEntity userGoalProgressEntity){

    }
}
