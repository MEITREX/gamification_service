package de.unistuttgart.iste.meitrex.gamification_service.service.functional;

import java.util.*;

import org.springframework.stereotype.*;

import org.springframework.beans.factory.annotation.*;

@Component
class DefaultXPImplementation implements IXPLevelMapping, IXPLevelDistance {

    // Validation

    private static final String ERR_MSG_XP_VALUE_MUST_BE_NON_NEGATIVE = "XP value must be non-negative.";

    private static final String ERR_MSG_LEVEL_MUST_BE_NON_NEGATIVE = "Level must be non-negative.";

    private static final String ERR_MSG_WEIGHT_MUST_BE_NON_NEGATIVE = "Multiplication weight must be non-negative.";

    private static final String ERR_MSG_UNSUPPORTED_LEVEL = "The passed level ist not supported by this implementation.";

    private static void assureNonNegative(double value, String msg) {
        if(value < 0) {
            throw new IllegalArgumentException(msg);
        }
    }

    private static void assureNonNegative(int value, String msg) {
        if(value < 0) {
            throw new IllegalArgumentException(msg);
        }
    }

    private static void assureIsSupportedLevel(int maxLevel, int curLevel, String msg) {
        if(maxLevel < curLevel) {
            throw new IllegalArgumentException(msg);
        }
    }

    // Initializes and builds the XP-to-level mapping

    private static Map<Integer, Double> createXPThresholdMap() {
        final Map<Integer, Double> levelXPThresholdMapping = new HashMap<>();
        levelXPThresholdMapping.put(0, 0.0);
        return levelXPThresholdMapping;
    }

    private static Map<Integer, Double> initXPThresholdMap(Map<Integer, Double> mapping, int maxLevel, double multiplicationWeight) {
        int curLevel = 1;
        double curXPThreshold = 0.0;
        while(curLevel < maxLevel) {
            curXPThreshold += multiplicationWeight * Math.log10((curLevel / 2.00) + 1);
            mapping.put(curLevel, curXPThreshold);
            curLevel++;
        }
        return mapping;
    }

    private static List<Map.Entry<Integer, Double>> createXPThresholdMappingList(Map<Integer, Double> mapping) {
        return mapping.entrySet()
                .stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue())
                .toList();
    }

    // Field variables

    private final int maxLevel;

    private final Map<Integer, Double> levelXPThresholdMapping;

    private final List<Map.Entry<Integer, Double>> levelXPThresholdMappingList;


    public DefaultXPImplementation(
            @Value("${de.unistuttgart.iste.meitrex.gamification_service.service.xp.maxLevel:40}") int maxLevel,
            @Value("${de.unistuttgart.iste.meitrex.gamification_service.service.xp.multiplicationWeight:600.0}") double multiplicationWeight
    ) {
        assureNonNegative(maxLevel, ERR_MSG_LEVEL_MUST_BE_NON_NEGATIVE);
        assureNonNegative(multiplicationWeight, ERR_MSG_WEIGHT_MUST_BE_NON_NEGATIVE);
        this.maxLevel = maxLevel;
        this.levelXPThresholdMapping = initXPThresholdMap(createXPThresholdMap(), maxLevel, multiplicationWeight);
        this.levelXPThresholdMappingList = createXPThresholdMappingList(this.levelXPThresholdMapping);
    }

    @Override
    public int calcLevel(double xpValue) {
        assureNonNegative(xpValue, ERR_MSG_XP_VALUE_MUST_BE_NON_NEGATIVE);
        int minIndex = 0;
        int maxIndex = levelXPThresholdMappingList.size() - 1;
        int level = 0;
        while (minIndex <= maxIndex) {
            int midIndex = (minIndex + maxIndex) / 2;
            double midThreshold = levelXPThresholdMappingList
                    .get(midIndex)
                    .getValue();
            if (xpValue >= midThreshold) {
                level = levelXPThresholdMappingList
                        .get(midIndex)
                        .getKey();
                minIndex = midIndex + 1;
            } else {
                maxIndex = midIndex - 1;
            }
        }
        return level;
    }

    @Override
    public double calcDistance(double curXP, int targetLevel) {
        assureIsSupportedLevel(this.maxLevel, targetLevel, ERR_MSG_UNSUPPORTED_LEVEL);
        return this.levelXPThresholdMapping.get(targetLevel) - curXP;
    }
}
