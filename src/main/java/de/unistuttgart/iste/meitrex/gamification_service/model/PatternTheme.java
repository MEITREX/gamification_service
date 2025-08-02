package de.unistuttgart.iste.meitrex.gamification_service.model;

import lombok.Data;

import java.util.UUID;

@Data
public class PatternTheme {
    private String foreColor;
    private String name;
    private String description;
    private ItemRarity rarity;
    private boolean obtainableInLottery;
    private boolean obtainableAsReward;
    private boolean obtainableInShop;
    private UUID id;
    private int moneyCost;
    private int sellCompensation;
    private String url;
}
