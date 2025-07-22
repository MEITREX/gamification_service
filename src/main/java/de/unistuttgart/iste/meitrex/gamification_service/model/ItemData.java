package de.unistuttgart.iste.meitrex.gamification_service.model;

import lombok.Data;

import java.util.List;

@Data
public class ItemData {

    private List<PatternTheme> patternThemes;
    private List<ColorTheme> colorThemes;
    private List<ProfilePicFrame> profilePicFrames;
    private List<ProfilePic> profilePics;
    private List<Tutor> tutors;
}
