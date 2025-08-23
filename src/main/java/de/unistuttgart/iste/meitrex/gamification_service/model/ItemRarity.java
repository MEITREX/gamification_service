package de.unistuttgart.iste.meitrex.gamification_service.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum ItemRarity {
    DEFAULT,
    COMMON,
    UNCOMMON,
    RARE,
    ULTRA_RARE;

    @JsonCreator
    public static ItemRarity fromValue(String value) {
        return ItemRarity.valueOf(value.toUpperCase());
    }
}
