package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.skilllevels;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.generated.dto.BloomLevel;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillLevelsEntity implements IWithId<UUID> {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private SkillEntity skill;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    private float valueRemember;
    private float valueUnderstand;
    private float valueApply;
    private float valueAnalyze;
    private float valueEvaluate;
    private float valueCreate;

    public SkillLevelsEntity(SkillEntity skill, UserEntity user) {
        this.skill = skill;
        this.user = user;
        this.valueRemember = 0.0f;
        this.valueUnderstand = 0.0f;
        this.valueApply = 0.0f;
        this.valueAnalyze = 0.0f;
        this.valueEvaluate = 0.0f;
        this.valueCreate = 0.0f;
    }

    public float getBloomLevelValue(final BloomLevel bloomLevel) {
        return switch (bloomLevel) {
            case REMEMBER -> valueRemember;
            case UNDERSTAND -> valueUnderstand;
            case APPLY -> valueApply;
            case ANALYZE -> valueAnalyze;
            case EVALUATE -> valueEvaluate;
            case CREATE -> valueCreate;
        };
    }

    public void setBloomLevelValue(final BloomLevel bloomLevel, final float value) {
        switch (bloomLevel) {
            case REMEMBER -> valueRemember = value;
            case UNDERSTAND -> valueUnderstand = value;
            case APPLY -> valueApply = value;
            case ANALYZE -> valueAnalyze = value;
            case EVALUATE -> valueEvaluate = value;
            case CREATE -> valueCreate = value;
        }
    }
}
