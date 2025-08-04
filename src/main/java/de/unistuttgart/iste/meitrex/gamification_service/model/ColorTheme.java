package de.unistuttgart.iste.meitrex.gamification_service.model;

import lombok.Data;

import java.util.UUID;

@Data
public class ColorTheme {
    private String foreColor;
    private String backColor;
    private String name;
    private String description;
    private String rarity;
    private boolean obtainableInLottery;
    private boolean obtainableAsReward;
    private boolean obtainableInShop;
    private UUID id;
    private int moneyCost;
    private int sellCompensation;
}
