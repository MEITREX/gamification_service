package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.model.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemInstanceEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemType;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.UserInventoryEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.ItemInstanceRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.UserRepository;
import de.unistuttgart.iste.meitrex.gamification_service.utility.ItemParser;
import de.unistuttgart.iste.meitrex.generated.dto.Inventory;
import de.unistuttgart.iste.meitrex.generated.dto.UserItem;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@Transactional
public class ItemService {
    private final UserRepository userRepository;
    private final GoalProgressService goalProgressService;
    private final ItemInstanceRepository itemInstanceRepository;

    @Value("${item.file.path}")
    private String FILE_PATH;

    private ItemData items;

    public ItemService(UserRepository userRepository, GoalProgressService goalProgressService, ItemInstanceRepository itemInstanceRepository) {
        this.userRepository = userRepository;
        this.goalProgressService = goalProgressService;
        this.itemInstanceRepository = itemInstanceRepository;
    }

    @PostConstruct
    public void init() {
        log.info("File path: {}", FILE_PATH);
        parseItemJson(FILE_PATH);
    }

    private void parseItemJson(String filePath) {
        try {
            log.info("Parsing JSON file with path {}", filePath);
            items = ItemParser.parseFromFile(filePath);
            log.info("Finished Parsing JSON file with path {}", filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Inventory getInventoryForUser(UUID userId) {
        UserEntity user = userRepository.findById(userId).orElseGet(() -> goalProgressService.createUser(userId));
        List<UserItem> userItems = getItemsForUser(userId);
        Inventory inventory = new Inventory();
        inventory.setItems(userItems);
        inventory.setUserId(userId);
        inventory.setUnspentPoints(user.getInventory().getUnspentPoints());
        return inventory;
    }

    public List<UserItem> getItemsForUser(UUID userId) {
        List<UserItem> userItems = new ArrayList<>();
        UserEntity user = userRepository.findById(userId).orElseGet(() -> goalProgressService.createUser(userId));
        getProfilePictureFrames(user, userItems);
        getProfilePictures(user, userItems);
        getTutorCharacters(user, userItems);
        getProfileColorThemes(user, userItems);
        getProfilePatterns(user, userItems);
        return userItems;
    }

    public Inventory buyItem(UUID userId, UUID itemId) {
        UserEntity user = userRepository.findById(userId).orElseGet(() -> goalProgressService.createUser(userId));
        if (user.getInventory().getItems().stream()
                .filter(itemInstanceEntity -> itemInstanceEntity.getPrototypeId().equals(itemId)).findAny().isEmpty()) {
            if (itemExists(itemId)) {
                ItemInstanceEntity itemInstanceEntity = new ItemInstanceEntity();
                itemInstanceEntity.setPrototypeId(itemId);
                itemInstanceEntity.setEquipped(false);
                itemInstanceEntity.setUniqueDescription("");
                itemInstanceEntity.setItemType(getItemType(itemId));
                user.getInventory().getItems().add(itemInstanceEntity);
                userRepository.save(user);
            }
        }
        return getInventoryForUser(userId);
    }

    public Inventory equipItem(UUID userId, UUID itemId) {
        UserEntity user = userRepository.findById(userId).orElseGet(() -> goalProgressService.createUser(userId));
        user.getInventory().getItems().stream().filter(itemInstanceEntity -> itemInstanceEntity.getPrototypeId().equals(itemId)).findFirst().ifPresent(itemInstanceEntity -> {
            user.getInventory().getItems().stream().filter(itemInstanceEntity1 ->
                    itemInstanceEntity1.getItemType().equals(itemInstanceEntity.getItemType()))
                    .forEach(itemInstanceEntity1 -> itemInstanceEntity1.setEquipped(false));
            userRepository.save(user);
            itemInstanceEntity.setEquipped(true);
            itemInstanceRepository.save(itemInstanceEntity);
        });
        return getInventoryForUser(userId);
    }

    public Inventory unequipItem(UUID userId, UUID itemId) {
        UserEntity user = userRepository.findById(userId).orElseGet(() -> goalProgressService.createUser(userId));
        user.getInventory().getItems().stream().filter(itemInstanceEntity -> itemInstanceEntity.getPrototypeId().equals(itemId)).findFirst().ifPresent(itemInstanceEntity -> {
           if (itemInstanceEntity.getItemType() != ItemType.Tutor) {
               itemInstanceEntity.setEquipped(false);
               itemInstanceRepository.save(itemInstanceEntity);
           }
        });
        return getInventoryForUser(userId);
    }

    private ItemType getItemType(UUID itemId) {
        if (items.getPatternThemes().stream().anyMatch(patternTheme -> patternTheme.getId().equals(itemId))) {
            return ItemType.PatternTheme;
        }
        if (items.getTutors().stream().anyMatch(tutor -> tutor.getId().equals(itemId))) {
            return ItemType.Tutor;
        }
        if (items.getColorThemes().stream().anyMatch(colorTheme -> colorTheme.getId().equals(itemId))) {
            return ItemType.ColorTheme;
        }
        if (items.getProfilePics().stream().anyMatch(profilePic -> profilePic.getId().equals(itemId))) {
            return ItemType.ProfilePic;
        }
        if (items.getProfilePicFrames().stream().anyMatch(profilePicFrame -> profilePicFrame.getId().equals(itemId))) {
            return ItemType.ProfilePicFrame;
        }
        return null;
    }

    private boolean itemExists(UUID itemId) {
        if (items.getPatternThemes().stream().anyMatch(patternTheme -> patternTheme.getId().equals(itemId))) {
            return true;
        }
        if (items.getTutors().stream().anyMatch(tutor -> tutor.getId().equals(itemId))) {
            return true;
        }
        if (items.getColorThemes().stream().anyMatch(colorTheme -> colorTheme.getId().equals(itemId))) {
            return true;
        }
        if (items.getProfilePics().stream().anyMatch(profilePic -> profilePic.getId().equals(itemId))) {
            return true;
        }
        return items.getProfilePicFrames().stream().anyMatch(profilePicFrame -> profilePicFrame.getId().equals(itemId));
    }

    private void getProfilePictureFrames(UserEntity user, List<UserItem> userItems) {
        items.getProfilePicFrames().forEach(profilePicFrame -> {
            getItems(user, profilePicFrame.getId(), userItems);
        });
    }

    private void getProfilePictures(UserEntity user, List<UserItem> userItems) {
        items.getProfilePics().forEach(profilePic -> {
            getItems(user, profilePic.getId(), userItems);
        });
    }

    private void getTutorCharacters(UserEntity user, List<UserItem> userItems) {
        items.getTutors().forEach(tutor -> {
            getItems(user, tutor.getId(), userItems);
        });
    }

    private void getProfileColorThemes(UserEntity user, List<UserItem> userItems) {
        items.getColorThemes().forEach(colorTheme -> {
            getItems(user, colorTheme.getId(), userItems);
        });
    }

    private void getProfilePatterns(UserEntity user, List<UserItem> userItems) {
        items.getPatternThemes().forEach(patternTheme -> {
            getItems(user, patternTheme.getId(), userItems);
        });
    }

    private static void getItems(UserEntity user, UUID itemId, List<UserItem> userItems) {
        if (user.getInventory() == null) {
            user.setInventory(new UserInventoryEntity());
        }
        ItemInstanceEntity itemInstance = user.getInventory().getItems().stream()
                .filter(itemInstanceEntity -> itemInstanceEntity.getPrototypeId().equals(itemId))
                .findFirst().orElse(null);

        UserItem item = UserItem.builder()
                .setId(itemId)
                .setUnlocked(false)
                .setUniqueDescription("")
                .setEquipped(false)
                .build();
        if (itemInstance != null) {
            item.setUnlocked(true);
            item.setUniqueDescription(itemInstance.getUniqueDescription());
            item.setEquipped(itemInstance.isEquipped());
        }
        userItems.add(item);
    }
}
