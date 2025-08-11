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
import de.unistuttgart.iste.meitrex.generated.dto.UserItemComplete;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
@Transactional
public class ItemService {
    private final UserRepository userRepository;
    private final GoalProgressService goalProgressService;
    private final ItemInstanceRepository itemInstanceRepository;

    @Value("${item.file.path}")
    private String FILE_PATH;

    private final Random r = new Random();

    private final static double COMMON_PERCENTAGE = 0.65;
    private final static double UNCOMMON_PERCENTAGE = 0.2;
    private final static double RARE_PERCENTAGE = 0.12;
    private final static double ULTRA_RARE_PERCENTAGE = 0.03;
    private final static int LOTTERY_COST = 3000;

    private final List<ItemParent> itemList;
    private List<ItemParent> commonLotteryItemList;
    private List<ItemParent> uncommonLotteryItemList;
    private List<ItemParent> rareLotteryItemList;
    private List<ItemParent> ultraRareLotteryItemList;

    public ItemService(UserRepository userRepository, GoalProgressService goalProgressService, ItemInstanceRepository itemInstanceRepository, List<ItemParent> lotteryItemList) {
        this.userRepository = userRepository;
        this.goalProgressService = goalProgressService;
        this.itemInstanceRepository = itemInstanceRepository;
        if (COMMON_PERCENTAGE + UNCOMMON_PERCENTAGE + RARE_PERCENTAGE + ULTRA_RARE_PERCENTAGE != 1.0) {
            log.warn("Lottery Percentages do not sum up to 1");
        }
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
            commonLotteryItemList = itemList.stream().filter(ItemParent::isObtainableInLottery)
                    .filter(itemParent -> itemParent.getRarity().equals(ItemRarity.COMMON)).toList();
            uncommonLotteryItemList = itemList.stream().filter(ItemParent::isObtainableInLottery)
                    .filter(itemParent -> itemParent.getRarity().equals(ItemRarity.UNCOMMON)).toList();
            rareLotteryItemList = itemList.stream().filter(ItemParent::isObtainableInLottery)
                    .filter(itemParent -> itemParent.getRarity().equals(ItemRarity.RARE)).toList();
            ultraRareLotteryItemList = itemList.stream().filter(ItemParent::isObtainableInLottery)
                    .filter(itemParent -> itemParent.getRarity().equals(ItemRarity.ULTRA_RARE)).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Inventory getInventoryForUser(UUID userId) {
        UserEntity user = userRepository.findById(userId).orElseGet(() -> goalProgressService.createUser(userId));
        if(user.getInventory().getItems().isEmpty()) {
            addDefaultItems(user);
        }
        List<UserItem> userItems = getItems(user);
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
                    if (user.getInventory().getUnspentPoints()>= itemParent.getMoneyCost()) {
                        user.getInventory().setUnspentPoints(user.getInventory().getUnspentPoints() - itemParent.getMoneyCost());
                        user.getInventory().getItems().add(itemParent.toItemInstance());
                        userRepository.save(user);
                    }
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

    public Inventory addItemRewardToUser(UUID userId, UUID itemId) {
        UserEntity user = userRepository.findById(userId).orElseGet(() -> goalProgressService.createUser(userId));
        if(user.getInventory().getItems().isEmpty()) {
            addDefaultItems(user);
        }
        itemList.stream().filter(ItemParent::isObtainableAsReward)
                .filter(itemParent -> itemParent.getId().equals(itemId))
                .findAny().ifPresent(itemParent -> {
                    if (user.getInventory().getItems().stream().filter(itemInstanceEntity ->
                            itemInstanceEntity.getPrototypeId().equals(itemId)).findAny().isEmpty()) {
                        user.getInventory().getItems().add(itemParent.toItemInstance());
                    } else {
                        user.getInventory().setUnspentPoints(user.getInventory().getUnspentPoints()
                                + itemParent.getSellCompensation());
                    }
                });
        userRepository.save(user);
        return getInventoryForUser(user);
    }

    public Inventory addPointsToUser(UUID userId, Integer points) {
        UserEntity user = userRepository.findById(userId).orElseGet(() -> goalProgressService.createUser(userId));
        if(user.getInventory().getItems().isEmpty()) {
            addDefaultItems(user);
        }
        user.getInventory().setUnspentPoints(user.getInventory().getUnspentPoints() + points);
        userRepository.save(user);
        return getInventoryForUser(user);
    }

    public UserItemComplete lotteryRun(UUID userId) {
        UserItemComplete userItem = new UserItemComplete();
        UserEntity user = userRepository.findById(userId).orElseGet(() -> goalProgressService.createUser(userId));
        if(user.getInventory().getItems().isEmpty()) {
            addDefaultItems(user);
        }
        if (user.getInventory().getUnspentPoints() <= LOTTERY_COST) {
            userItem.setDescription("Not enough money");
            return userItem;
        } else {
            user.getInventory().setUnspentPoints(user.getInventory().getUnspentPoints() - LOTTERY_COST);
        }
        if(user.getInventory().getItems().isEmpty()) {
            addDefaultItems(user);
        }
        double randomValue = r.nextDouble();
        if (randomValue < COMMON_PERCENTAGE) {
            userItem = addItemToUser(user, commonLotteryItemList);
        } else if (randomValue < COMMON_PERCENTAGE + UNCOMMON_PERCENTAGE) {
            userItem = addItemToUser(user, uncommonLotteryItemList);
        } else if (randomValue < COMMON_PERCENTAGE + UNCOMMON_PERCENTAGE + RARE_PERCENTAGE) {
            userItem = addItemToUser(user, rareLotteryItemList);
        } else {
            userItem = addItemToUser(user, ultraRareLotteryItemList);
        }
        return userItem;
    }

    @NotNull
    private UserItemComplete addItemToUser(UserEntity user, List<ItemParent> rareLotteryItemList) {
        UserItemComplete userItem;
        ItemParent item = rareLotteryItemList.get(r.nextInt(rareLotteryItemList.size()));
        Optional<ItemInstanceEntity> oldItemInstance = user.getInventory().getItems().stream()
                .filter(itemInstanceEntity -> itemInstanceEntity.getPrototypeId().equals(item.getId())).findFirst();
        if (oldItemInstance.isPresent()) {
            user.getInventory().setUnspentPoints(user.getInventory().getUnspentPoints() + item.getSellCompensation());
            userItem = item.toCompleteUserItemInstance();
            userItem.setSold(true);
            userItem.setUnlockedTime(oldItemInstance.get().getCreationTime());
        } else {
            ItemInstanceEntity itemInstance = item.toItemInstance();
            user.getInventory().getItems().add(itemInstance);
            userItem = item.toCompleteUserItemInstance();
            userItem.setUnlockedTime(itemInstance.getCreationTime());
            userItem.setSold(false);
        }
        userItem.setEquipped(false);
        userItem.setUnlocked(true);
        return userItem;
    }

    private Inventory getInventoryForUser(UserEntity user) {
        List<UserItem> userItems = getItems(user);
        Inventory inventory = new Inventory();
        inventory.setItems(userItems);
        inventory.setUserId(user.getId());
        inventory.setUnspentPoints(user.getInventory().getUnspentPoints());
        return inventory;
    }

    private void addDefaultItems(UserEntity user) {
        itemList.stream().filter(itemParent -> itemParent.getRarity().equals(ItemRarity.DEFAULT)).forEach(itemParent -> {
            user.getInventory().getItems().add(itemParent.toItemInstance());
        });
        user.getInventory().getItems().forEach(itemInstanceEntity -> {
            itemInstanceEntity.setEquipped(true);
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
            item.setUnlockedTime(itemInstance.getCreationTime());
            item.setUniqueDescription(itemInstance.getUniqueDescription());
            item.setEquipped(itemInstance.isEquipped());
        }
        userItems.add(item);
    }
}
