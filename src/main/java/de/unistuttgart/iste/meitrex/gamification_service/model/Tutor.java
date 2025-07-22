package de.unistuttgart.iste.meitrex.gamification_service.model;

import lombok.Data;

import java.util.UUID;

@Data
public class Tutor {
    private String filename;
    private String name;
    private String nickname;
    private String description;
    private String rarity;
    private boolean obtainableInLottery;
    private boolean obtainableAsReward;
    private boolean obtainableInShop;
    private UUID id;
    private int moneyCost;
    private int sellCompensation;
}
