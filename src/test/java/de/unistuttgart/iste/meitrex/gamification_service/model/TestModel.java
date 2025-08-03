package de.unistuttgart.iste.meitrex.gamification_service.model;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemInstanceEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemType;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TestModel {

    @Test
    void testColorThemeToItemInstance() {
        ColorTheme colorTheme = new ColorTheme();
        colorTheme.setBackColor("backgroundColor");
        colorTheme.setForeColor("foregroundColor");
        colorTheme.setName("ColorTheme");
        colorTheme.setDescription("Description");
        colorTheme.setId(UUID.randomUUID());
        colorTheme.setMoneyCost(1000);
        colorTheme.setObtainableAsReward(true);
        colorTheme.setObtainableInLottery(true);
        colorTheme.setObtainableInShop(true);
        colorTheme.setSellCompensation(500);
        colorTheme.setRarity(ItemRarity.COMMON);
        ItemInstanceEntity itemInstanceEntity = colorTheme.toItemInstance();
        assertThat(itemInstanceEntity.getItemType(), is(ItemType.ColorTheme));
        assertThat(itemInstanceEntity.getPrototypeId(), is(colorTheme.getId()));
    }

    @Test
    void testPatternThemeToItemInstance() {
        PatternTheme patternTheme = new PatternTheme();
        patternTheme.setUrl("Url");
        patternTheme.setForeColor("foregroundColor");
        patternTheme.setName("PatternTheme");
        patternTheme.setDescription("Description");
        patternTheme.setId(UUID.randomUUID());
        patternTheme.setMoneyCost(1000);
        patternTheme.setObtainableAsReward(true);
        patternTheme.setObtainableInLottery(true);
        patternTheme.setObtainableInShop(true);
        patternTheme.setSellCompensation(500);
        patternTheme.setRarity(ItemRarity.COMMON);
        ItemInstanceEntity itemInstanceEntity = patternTheme.toItemInstance();
        assertThat(itemInstanceEntity.getItemType(), is(ItemType.PatternTheme));
        assertThat(itemInstanceEntity.getPrototypeId(), is(patternTheme.getId()));
    }

    @Test
    void testProfilePicToItemInstance() {
        ProfilePic profilePic = new ProfilePic();
        profilePic.setUrl("Url");
        profilePic.setName("ProfilePic");
        profilePic.setDescription("Description");
        profilePic.setId(UUID.randomUUID());
        profilePic.setMoneyCost(1000);
        profilePic.setObtainableAsReward(true);
        profilePic.setObtainableInLottery(true);
        profilePic.setObtainableInShop(true);
        profilePic.setSellCompensation(500);
        profilePic.setRarity(ItemRarity.COMMON);
        ItemInstanceEntity itemInstanceEntity = profilePic.toItemInstance();
        assertThat(itemInstanceEntity.getItemType(), is(ItemType.ProfilePic));
        assertThat(itemInstanceEntity.getPrototypeId(), is(profilePic.getId()));
    }

    @Test
    void testProfilePicFrameToItemInstance() {
        ProfilePicFrame profilePicFrame = new ProfilePicFrame();
        profilePicFrame.setUrl("Url");
        profilePicFrame.setName("ProfilePicFrame");
        profilePicFrame.setDescription("Description");
        profilePicFrame.setId(UUID.randomUUID());
        profilePicFrame.setMoneyCost(1000);
        profilePicFrame.setObtainableAsReward(true);
        profilePicFrame.setObtainableInLottery(true);
        profilePicFrame.setObtainableInShop(true);
        profilePicFrame.setSellCompensation(500);
        profilePicFrame.setRarity(ItemRarity.COMMON);
        ItemInstanceEntity itemInstanceEntity = profilePicFrame.toItemInstance();
        assertThat(itemInstanceEntity.getItemType(), is(ItemType.ProfilePicFrame));
        assertThat(itemInstanceEntity.getPrototypeId(), is(profilePicFrame.getId()));
    }

    @Test
    void testTutorToItemInstance() {
        Tutor tutor = new Tutor();
        tutor.setName("Tutor");
        tutor.setDescription("Description");
        tutor.setId(UUID.randomUUID());
        tutor.setMoneyCost(1000);
        tutor.setObtainableAsReward(true);
        tutor.setObtainableInLottery(true);
        tutor.setObtainableInShop(true);
        tutor.setSellCompensation(500);
        tutor.setRarity(ItemRarity.COMMON);
        ItemInstanceEntity itemInstanceEntity = tutor.toItemInstance();
        assertThat(itemInstanceEntity.getItemType(), is(ItemType.Tutor));
        assertThat(itemInstanceEntity.getPrototypeId(), is(tutor.getId()));
    }
}
