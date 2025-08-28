package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.model.ItemData;
import de.unistuttgart.iste.meitrex.gamification_service.model.ItemParent;
import de.unistuttgart.iste.meitrex.gamification_service.model.ItemRarity;
import de.unistuttgart.iste.meitrex.gamification_service.model.access.IItemProvider;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemInstanceEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemType;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.UserInventoryEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.ItemInstanceRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.generated.dto.Inventory;
import de.unistuttgart.iste.meitrex.generated.dto.UserItem;
import de.unistuttgart.iste.meitrex.generated.dto.UserItemComplete;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class DefaultItemService implements IItemService {

    // Static Constants

    private final static double COMMON_PERCENTAGE = 0.65;

    private final static double UNCOMMON_PERCENTAGE = 0.2;

    private final static double RARE_PERCENTAGE = 0.12;

    private final static double ULTRA_RARE_PERCENTAGE = 0.03;

    private final static int LOTTERY_COST = 3000;

    // Initialization Logic

    private static void initItemLists(IItemProvider itemProvider, DefaultItemService itemService) {
        try {
            final ItemData itemData = itemProvider.load();
            itemService.itemList.addAll(itemData.getColorThemes());
            itemService.itemList.addAll(itemData.getTutors());
            itemService.itemList.addAll(itemData.getProfilePics());
            itemService.itemList.addAll(itemData.getPatternThemes());
            itemService.itemList.addAll(itemData.getProfilePicFrames());
            itemService.commonLotteryItemList = itemService
                    .itemList
                    .stream()
                    .filter(ItemParent::isObtainableInLottery)
                    .filter(itemParent -> itemParent.getRarity().equals(ItemRarity.COMMON))
                    .toList();
            itemService.uncommonLotteryItemList = itemService
                    .itemList
                    .stream()
                    .filter(ItemParent::isObtainableInLottery)
                    .filter(itemParent -> itemParent.getRarity().equals(ItemRarity.UNCOMMON))
                    .toList();
            itemService.rareLotteryItemList = itemService
                    .itemList
                    .stream()
                    .filter(ItemParent::isObtainableInLottery)
                    .filter(itemParent -> itemParent.getRarity().equals(ItemRarity.RARE))
                    .toList();
            itemService.ultraRareLotteryItemList = itemService
                    .itemList
                    .stream()
                    .filter(ItemParent::isObtainableInLottery)
                    .filter(itemParent -> itemParent.getRarity().equals(ItemRarity.ULTRA_RARE))
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    //private final UserService userService;

    private final IUserCreator userCreator;

    private final IUserRepository userRepository;

    private final ItemInstanceRepository itemInstanceRepository;

    private final IItemProvider itemProvider = null;

    // Data Structures

    private final Random r = new Random();

    private final List<ItemParent> itemList;

    private List<ItemParent> commonLotteryItemList;

    private List<ItemParent> uncommonLotteryItemList;

    private List<ItemParent> rareLotteryItemList;

    private List<ItemParent> ultraRareLotteryItemList;


    public DefaultItemService(@Autowired IItemProvider itemProvider, @Autowired IUserCreator userCreator, @Autowired IUserRepository userRepository, @Autowired ItemInstanceRepository itemInstanceRepository) {
        this.userCreator = Objects.requireNonNull(userCreator);
        this.userRepository = Objects.requireNonNull(userRepository);
        this.itemInstanceRepository = Objects.requireNonNull(itemInstanceRepository);
        itemList = new ArrayList<>();
        initItemLists(itemProvider, this);
    }


    public Inventory getInventoryForUser(UUID userId) {
        UserEntity user = userCreator.fetchOrCreate(userId);
        if (user.getInventory() == null) {
            user.setInventory(new UserInventoryEntity());
        }
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
        UserEntity user = userCreator.fetchOrCreate(userId);
        if (user.getInventory() == null) {
            user.setInventory(new UserInventoryEntity());
        }
        if(user.getInventory().getItems().isEmpty()) {
            addDefaultItems(user);
        }
        return getItems(user);
    }

    public Inventory buyItem(UUID userId, UUID itemId) {
        UserEntity user = userCreator.fetchOrCreate(userId);
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
                            // TODO Replaced upsert by save
                            userRepository.save(user);
                        }
                    });
        }
        return getInventoryForUser(user);
    }

    public Inventory equipItem(UUID userId, UUID itemId) {
        UserEntity user = userCreator.fetchOrCreate(userId);
        if(user.getInventory().getItems().isEmpty()) {
            addDefaultItems(user);
        }
        user.getInventory().getItems().stream().filter(itemInstanceEntity -> itemInstanceEntity.getPrototypeId()
                .equals(itemId)).findFirst().ifPresent(itemInstanceEntity -> {
            user.getInventory().getItems().stream().filter(itemInstanceEntity1 ->
                            itemInstanceEntity1.getItemType().equals(itemInstanceEntity.getItemType()))
                    .forEach(itemInstanceEntity1 -> itemInstanceEntity1.setEquipped(false));
            if (itemInstanceEntity.getItemType() == ItemType.ColorTheme) {
                user.getInventory().getItems().stream().filter(itemInstanceEntity1 -> itemInstanceEntity1.getItemType()
                        .equals(ItemType.PatternTheme)).forEach(itemInstanceEntity1 -> {itemInstanceEntity1.setEquipped(false);});
            }
            if (itemInstanceEntity.getItemType() == ItemType.PatternTheme) {
                user.getInventory().getItems().stream().filter(itemInstanceEntity1 -> itemInstanceEntity1.getItemType()
                        .equals(ItemType.ColorTheme)).forEach(itemInstanceEntity1 -> {itemInstanceEntity1.setEquipped(false);});
            }
            userRepository.save(user);
            itemInstanceEntity.setEquipped(true);
            itemInstanceRepository.save(itemInstanceEntity);
        });
        return getInventoryForUser(user);
    }

    public Inventory unequipItem(UUID userId, UUID itemId) {
        UserEntity user = userCreator.fetchOrCreate(userId);
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
        UserEntity user = userCreator.fetchOrCreate(userId);
        if (user.getInventory() == null) {
            user.setInventory(new UserInventoryEntity());
        }
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
        UserEntity user = userCreator.fetchOrCreate(userId);
        if(user.getInventory().getItems().isEmpty()) {
            addDefaultItems(user);
        }
        user.getInventory().setUnspentPoints(user.getInventory().getUnspentPoints() + points);
        userRepository.save(user);
        return getInventoryForUser(user);
    }

    public UserItemComplete lotteryRun(UUID userId) {
        UserItemComplete userItem;
        UserEntity user = userCreator.fetchOrCreate(userId);
        if (user.getInventory() == null) {
            user.setInventory(new UserInventoryEntity());
        }
        if(user.getInventory().getItems().isEmpty()) {
            addDefaultItems(user);
        }
        if (user.getInventory().getUnspentPoints() <= LOTTERY_COST) {
            return null;
        } else {
            user.getInventory().setUnspentPoints(user.getInventory().getUnspentPoints() - LOTTERY_COST);
        }
        if(user.getInventory().getItems().isEmpty()) {
            addDefaultItems(user);
        }
        double randomValue = r.nextDouble();
        if (randomValue < COMMON_PERCENTAGE) {
            userItem = addRandomItemToUser(user, commonLotteryItemList);
        } else if (randomValue < COMMON_PERCENTAGE + UNCOMMON_PERCENTAGE) {
            userItem = addRandomItemToUser(user, uncommonLotteryItemList);
        } else if (randomValue < COMMON_PERCENTAGE + UNCOMMON_PERCENTAGE + RARE_PERCENTAGE) {
            userItem = addRandomItemToUser(user, rareLotteryItemList);
        } else {
            userItem = addRandomItemToUser(user, ultraRareLotteryItemList);
        }
        return userItem;
    }


    @NotNull
    private UserItemComplete addRandomItemToUser(UserEntity user, List<ItemParent> lotteryItemList) {
        UserItemComplete userItem;
        ItemParent item = lotteryItemList.get(r.nextInt(lotteryItemList.size()));
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
        userRepository.save(user);
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
