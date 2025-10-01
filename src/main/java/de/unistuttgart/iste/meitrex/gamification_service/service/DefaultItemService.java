package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.domain.UserInventoryFactory;
import de.unistuttgart.iste.meitrex.gamification_service.model.ItemData;
import de.unistuttgart.iste.meitrex.gamification_service.model.ItemParent;
import de.unistuttgart.iste.meitrex.gamification_service.model.ItemRarity;
import de.unistuttgart.iste.meitrex.gamification_service.model.access.IItemProvider;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemInstanceEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemType;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.ItemInstanceRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.generated.dto.Inventory;
import de.unistuttgart.iste.meitrex.generated.dto.UserItem;
import de.unistuttgart.iste.meitrex.generated.dto.UserItemComplete;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Transactional
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
        } catch (Exception e) {
            //throw new IllegalStateException(e);
        }
    }

    //private final UserService userService;

    private final IGoalProgressService goalProgressService;

    private final IUserCreator userCreator;

    private final IUserRepository userRepository;

    private final ItemInstanceRepository itemInstanceRepository;

    private final IItemProvider itemProvider = null;

    private static UserInventoryFactory userInventoryFactory;

    // Data Structures

    private final Random r = new Random();

    private final List<ItemParent> itemList;

    private List<ItemParent> commonLotteryItemList;

    private List<ItemParent> uncommonLotteryItemList;

    private List<ItemParent> rareLotteryItemList;

    private List<ItemParent> ultraRareLotteryItemList;


    public DefaultItemService(@Autowired IItemProvider itemProvider, @Autowired IGoalProgressService goalProgressService, @Autowired IUserCreator userCreator, @Autowired IUserRepository userRepository, @Autowired ItemInstanceRepository itemInstanceRepository, @Autowired UserInventoryFactory userInventoryFactory) {
        this.goalProgressService = Objects.requireNonNull(goalProgressService);
        this.userCreator = Objects.requireNonNull(userCreator);
        this.userRepository = Objects.requireNonNull(userRepository);
        this.itemInstanceRepository = Objects.requireNonNull(itemInstanceRepository);
        DefaultItemService.userInventoryFactory = Objects.requireNonNull(userInventoryFactory);
        itemList = new ArrayList<>();
        initItemLists(itemProvider, this);
    }


    public Inventory getInventoryForUser(UUID userId) {
        UserEntity user = userCreator.fetchOrCreate(userId);
        ensureInventory(user);
        checkDefaultItems(user);
        List<UserItem> userItems = getItems(user);
        Inventory inventory = new Inventory();
        inventory.setItems(userItems);
        inventory.setUserId(userId);
        inventory.setUnspentPoints(user.getInventory().getUnspentPoints());
        return inventory;
    }

    public List<UserItem> getItemsForUser(UUID userId) {
        UserEntity user = userCreator.fetchOrCreate(userId);
        ensureInventory(user);
        checkDefaultItems(user);
        return getItems(user);
    }

    public List<Inventory> getInventoriesForUsers(List<UUID> userIds) {
        List<Inventory> inventories = new ArrayList<>();
        for (UUID userId : userIds) {
            Inventory inventory = getInventoryForUser(userId);
            inventory.setUnspentPoints(0);
            inventories.add(inventory);
        }
        return inventories;
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
                            user.getInventory().removePoints(itemParent.getMoneyCost());
                            user.getInventory().getItems().add(itemParent.toItemInstance());
                            // TODO Replaced upsert by save
                            userRepository.save(user);
                            goalProgressService.itemReceivedProgress(user);
                        }
                    });
        }
        return getInventoryForUser(user);
    }

    public Inventory equipItem(UUID userId, UUID itemId) {
        UserEntity user = userCreator.fetchOrCreate(userId);
        checkDefaultItems(user);
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
            goalProgressService.equipItemProgress(user);
        });
        return getInventoryForUser(user);
    }

    public Inventory unequipItem(UUID userId, UUID itemId) {
        UserEntity user = userCreator.fetchOrCreate(userId);
        checkDefaultItems(user);
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
        ensureInventory(user);
        checkDefaultItems(user);
        itemList.stream().filter(ItemParent::isObtainableAsReward)
                .filter(itemParent -> itemParent.getId().equals(itemId))
                .findAny().ifPresent(itemParent -> {
                    if (user.getInventory().getItems().stream().filter(itemInstanceEntity ->
                            itemInstanceEntity.getPrototypeId().equals(itemId)).findAny().isEmpty()) {
                        user.getInventory().getItems().add(itemParent.toItemInstance());
                        goalProgressService.itemReceivedProgress(user);
                    } else {
                        user.getInventory().addPoints(itemParent.getSellCompensation());
                    }
                });
        userRepository.save(user);
        return getInventoryForUser(user);
    }

    public Inventory addPointsToUser(UUID userId, Integer points) {
        UserEntity user = userCreator.fetchOrCreate(userId);
        checkDefaultItems(user);
        user.getInventory().addPoints(points);
        userRepository.save(user);
        return getInventoryForUser(user);
    }

    public UserItemComplete lotteryRun(UUID userId) {
        UserItemComplete userItem = new UserItemComplete();
        UserEntity user = userCreator.fetchOrCreate(userId);
        if (user.getInventory() == null) {
            user.setInventory(userInventoryFactory.createUserInventory());
        }
        checkDefaultItems(user);
        if (user.getInventory().getUnspentPoints() <= LOTTERY_COST) {
            return null;
        } else {
            user.getInventory().removePoints(LOTTERY_COST);
        }
        userItem = randomItemRun(userItem, user);
        goalProgressService.lotteryRunProgress(user);
        return userItem;
    }

    public void submissionReward(UUID userId) {
        UserItemComplete userItem = new UserItemComplete();
        UserEntity user = userCreator.fetchOrCreate(userId);
        if (user.getInventory() == null) {
            user.setInventory(userInventoryFactory.createUserInventory());
        }
        checkDefaultItems(user);
        if (r.nextBoolean()) {
            randomItemRun(userItem, user);
        }
        user.getInventory().addPoints(500);
        userRepository.save(user);
    }

    private UserItemComplete randomItemRun(UserItemComplete userItem, UserEntity user) {
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

    @Override
    public Optional<ItemParent> getItemPrototypeById(UUID prototypeId) {
        return itemList.stream()
                .filter(it -> it.getId().equals(prototypeId))
                .findFirst();
    }


    @NotNull
    private UserItemComplete addRandomItemToUser(UserEntity user, List<ItemParent> lotteryItemList) {
        UserItemComplete userItem;
        ItemParent item = lotteryItemList.get(r.nextInt(lotteryItemList.size()));
        Optional<ItemInstanceEntity> oldItemInstance = user.getInventory().getItems().stream()
                .filter(itemInstanceEntity -> itemInstanceEntity.getPrototypeId().equals(item.getId())).findFirst();
        if (oldItemInstance.isPresent()) {
            user.getInventory().addPoints(item.getSellCompensation());
            userItem = item.toCompleteUserItemInstance();
            userItem.setSold(true);
            userItem.setUnlockedTime(oldItemInstance.get().getCreationTime());
        } else {
            ItemInstanceEntity itemInstance = item.toItemInstance();
            user.getInventory().getItems().add(itemInstance);
            userItem = item.toCompleteUserItemInstance();
            userItem.setUnlockedTime(itemInstance.getCreationTime());
            userItem.setSold(false);
            goalProgressService.itemReceivedProgress(user);
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
        ensureInventory(user);
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

    private static void ensureInventory(UserEntity user) {
        if (user.getInventory() == null) {
            user.setInventory(userInventoryFactory.createUserInventory());
        }
    }

    private void checkDefaultItems(UserEntity user) {
        if(user.getInventory().getItems().isEmpty()) {
            addDefaultItems(user);
        }
    }
}
