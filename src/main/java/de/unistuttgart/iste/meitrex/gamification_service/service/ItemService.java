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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    private final List<ItemParent> itemList;

    public ItemService(UserRepository userRepository, GoalProgressService goalProgressService, ItemInstanceRepository itemInstanceRepository) {
        this.userRepository = userRepository;
        this.goalProgressService = goalProgressService;
        this.itemInstanceRepository = itemInstanceRepository;
        itemList = new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        log.info("File path: {}", FILE_PATH);
        parseItemJson(FILE_PATH);
    }

    private void parseItemJson(String filePath) {
        try {
            log.info("Parsing JSON file with path {}", filePath);
            ItemData items = ItemParser.parseFromFile(filePath);
            log.info("Finished Parsing JSON file with path {}", filePath);
            itemList.addAll(items.getColorThemes());
            itemList.addAll(items.getTutors());
            itemList.addAll(items.getProfilePics());
            itemList.addAll(items.getPatternThemes());
            itemList.addAll(items.getProfilePicFrames());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Inventory getInventoryForUser(UUID userId) {
        UserEntity user = userRepository.findById(userId).orElseGet(() -> goalProgressService.createUser(userId));
        if(user.getInventory().getItems().isEmpty()) {
            addDefaultItems(user);
        }
        List<UserItem> userItems = getItemsForUser(user);
        Inventory inventory = new Inventory();
        inventory.setItems(userItems);
        inventory.setUserId(userId);
        inventory.setUnspentPoints(user.getInventory().getUnspentPoints());
        return inventory;
    }

    public List<UserItem> getItemsForUser(UUID userId) {
        UserEntity user = userRepository.findById(userId).orElseGet(() -> goalProgressService.createUser(userId));
        if(user.getInventory().getItems().isEmpty()) {
            addDefaultItems(user);
        }
        return getItems(user);
    }

    public Inventory buyItem(UUID userId, UUID itemId) {
        UserEntity user = userRepository.findById(userId).orElseGet(() -> goalProgressService.createUser(userId));
        if(user.getInventory().getItems().isEmpty()) {
            addDefaultItems(user);
        }
        if (user.getInventory().getItems().stream()
                .filter(itemInstanceEntity -> itemInstanceEntity.getPrototypeId().equals(itemId)).findAny().isEmpty()) {
            itemList.stream().filter(itemParent -> itemParent.getId().equals(itemId))
                .findAny().ifPresent(itemParent -> {
                    user.getInventory().getItems().add(itemParent.toItemInstance());
                    userRepository.save(user);
            });
        }
        return getInventoryForUser(user);
    }

    public Inventory equipItem(UUID userId, UUID itemId) {
        UserEntity user = userRepository.findById(userId).orElseGet(() -> goalProgressService.createUser(userId));
        if(user.getInventory().getItems().isEmpty()) {
            addDefaultItems(user);
        }
        user.getInventory().getItems().stream().filter(itemInstanceEntity -> itemInstanceEntity.getPrototypeId().equals(itemId)).findFirst().ifPresent(itemInstanceEntity -> {
            user.getInventory().getItems().stream().filter(itemInstanceEntity1 ->
                    itemInstanceEntity1.getItemType().equals(itemInstanceEntity.getItemType()))
                    .forEach(itemInstanceEntity1 -> itemInstanceEntity1.setEquipped(false));
            userRepository.save(user);
            itemInstanceEntity.setEquipped(true);
            itemInstanceRepository.save(itemInstanceEntity);
        });
        return getInventoryForUser(user);
    }

    public Inventory unequipItem(UUID userId, UUID itemId) {
        UserEntity user = userRepository.findById(userId).orElseGet(() -> goalProgressService.createUser(userId));
        if(user.getInventory().getItems().isEmpty()) {
            addDefaultItems(user);
        }
        user.getInventory().getItems().stream().filter(itemInstanceEntity -> itemInstanceEntity.getPrototypeId().equals(itemId)).findFirst().ifPresent(itemInstanceEntity -> {
           if (itemInstanceEntity.getItemType() != ItemType.Tutor) {
               itemInstanceEntity.setEquipped(false);
               itemInstanceRepository.save(itemInstanceEntity);
           }
        });
        return getInventoryForUser(user);
    }

    public Inventory addItemToUser(UUID userId, UUID itemId) {
        UserEntity user = userRepository.findById(userId).orElseGet(() -> goalProgressService.createUser(userId));
        if(user.getInventory().getItems().isEmpty()) {
            addDefaultItems(user);
        }
        if (user.getInventory().getItems().stream().filter(itemInstanceEntity -> itemInstanceEntity.getPrototypeId().equals(itemId)).findAny().isEmpty()) {

        }
        return getInventoryForUser(user);
    }

    public Inventory addPointsToUser(UUID userId, Integer points) {
        UserEntity user = userRepository.findById(userId).orElseGet(() -> goalProgressService.createUser(userId));
        if(user.getInventory().getItems().isEmpty()) {
            addDefaultItems(user);
        }
        return getInventoryForUser(user);
    }

    private Inventory getInventoryForUser(UserEntity user) {
        List<UserItem> userItems = getItemsForUser(user);
        Inventory inventory = new Inventory();
        inventory.setItems(userItems);
        inventory.setUserId(user.getId());
        inventory.setUnspentPoints(user.getInventory().getUnspentPoints());
        return inventory;
    }

    private List<UserItem> getItemsForUser(UserEntity user) {
        List<UserItem> userItems = new ArrayList<>();
        getProfilePictureFrames(user, userItems);
        getProfilePictures(user, userItems);
        getTutorCharacters(user, userItems);
        getProfileColorThemes(user, userItems);
        getProfilePatterns(user, userItems);
        return userItems;
    }

    private void addDefaultItems(UserEntity user) {
        itemList.stream().filter(itemParent -> itemParent.getRarity().equals(ItemRarity.DEFAULT)).forEach(itemParent -> {
            user.getInventory().getItems().add(itemParent.toItemInstance());
        });
        userRepository.save(user);
    }

    private List<UserItem> getItems(UserEntity user) {
        List<UserItem> userItems = new ArrayList<>();
        itemList.forEach(itemParent -> {
            getItem(user, itemParent.getId(), userItems);
        });
        return userItems;
    }

    private static void getItem(UserEntity user, UUID itemId, List<UserItem> userItems) {
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
