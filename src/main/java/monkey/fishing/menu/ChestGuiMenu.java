package monkey.fishing.menu;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.List;
import monkey.fishing.MonkeyFishingMod;
import monkey.fishing.config.ConfigManager;
import monkey.fishing.config.CustomItemParser;
import monkey.fishing.config.MonkeyFishingConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

/** Real, server-synced chest menu backing the Monkey Fishing chest GUIs (owns its own MenuType so it isn't drawn by vanilla's chest screen). */
public class ChestGuiMenu extends ChestMenu {
    public static final MenuType<ChestGuiMenu> TYPE = Registry.register(
        BuiltInRegistries.MENU, MonkeyFishingMod.id("chest_gui"),
        new MenuType<>(ChestGuiMenu::new, FeatureFlags.VANILLA_SET)
    );

    private static final int SLOT_COUNT = 27;

    public enum Mode {
        HUB, SETTINGS, REWARDS, TRASH
    }

    private final Container container;
    private final Mode mode;

    public ChestGuiMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(SLOT_COUNT), Mode.HUB);
    }

    public ChestGuiMenu(int syncId, Inventory playerInventory, Container container) {
        this(syncId, playerInventory, container, Mode.HUB);
    }

    public ChestGuiMenu(int syncId, Inventory playerInventory, Container container, Mode mode) {
        super(TYPE, syncId, playerInventory, container, 3);
        this.container = container;
        this.mode = mode;
    }

    public static void open(ServerPlayer player, Mode mode) {
        Container container = switch (mode) {
            case HUB -> buildHub();
            case SETTINGS -> buildSettings();
            case REWARDS -> buildContainer(player.level().registryAccess(), mode);
            case TRASH -> buildContainer(player.level().registryAccess(), mode);
        };
        Component title = switch (mode) {
            case HUB -> Component.literal("Monkey Fishing");
            case SETTINGS -> Component.literal("Fishing Settings");
            case REWARDS -> Component.literal("Reward Loot");
            case TRASH -> Component.literal("Trash Loot");
        };
        player.openMenu(new net.minecraft.world.SimpleMenuProvider(
            (syncId, inventory, ignored) -> new ChestGuiMenu(syncId, inventory, container, mode), title
        ));
    }

    private static Container buildHub() {
        SimpleContainer container = new SimpleContainer(SLOT_COUNT);
        setButton(container, 11, Items.COMPASS, "Settings", "Adjust fishing chances and zone sizes");
        setButton(container, 13, Items.CHEST, "Reward Loot", "Edit items caught in the green zone");
        setButton(container, 15, Items.BARREL, "Trash Loot", "Edit items caught in the yellow zone");
        return container;
    }

    private static Container buildSettings() {
        SimpleContainer container = new SimpleContainer(SLOT_COUNT);
        setButton(container, 0, Items.ARROW, "Back", "Return to Monkey Fishing");

        setButton(container, 3, Items.REDSTONE, "Decrease by 5%", "Treasure chance");
        setSettingButton(container, 4, Items.FISHING_ROD, "Treasure chance", ConfigManager.CONFIG.treasureChancePercent, "%");
        setButton(container, 5, Items.GLOWSTONE_DUST, "Increase by 5%", "Treasure chance");

        setButton(container, 12, Items.REDSTONE, "Decrease by 1%", "Green zone size");
        setSettingButton(container, 13, Items.SLIME_BALL, "Green zone size", ConfigManager.CONFIG.greenZoneSizePercent, "%");
        setButton(container, 14, Items.GLOWSTONE_DUST, "Increase by 1%", "Green zone size");

        setButton(container, 21, Items.REDSTONE, "Decrease by 1%", "Yellow zone size");
        setSettingButton(container, 22, Items.GOLD_NUGGET, "Yellow zone per side", ConfigManager.CONFIG.yellowZoneSizePercent, "%");
        setButton(container, 23, Items.GLOWSTONE_DUST, "Increase by 1%", "Yellow zone size");
        return container;
    }

    private static void setSettingButton(Container container, int slot, Item icon, String name, int value, String suffix) {
        ItemStack stack = namedStack(icon, name + ": " + value + suffix);
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("Use the adjacent buttons to adjust"))));
        container.setItem(slot, stack);
    }

    private static void setButton(Container container, int slot, Item icon, String name, String detail) {
        ItemStack stack = namedStack(icon, name);
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal(detail))));
        container.setItem(slot, stack);
    }

    private static ItemStack namedStack(Item item, String name) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }

    private static List<MonkeyFishingConfig.CustomLootEntry> entriesFor(Mode mode) {
        return mode == Mode.TRASH ? ConfigManager.CONFIG.trashItems : ConfigManager.CONFIG.customItems;
    }

    public static Container buildContainer(HolderLookup.Provider registries) {
        return buildContainer(registries, Mode.REWARDS);
    }

    public static Container buildContainer(HolderLookup.Provider registries, Mode mode) {
        SimpleContainer container = new SimpleContainer(SLOT_COUNT);
        var items = entriesFor(mode);
        for (int i = 0; i < items.size() && i < container.getContainerSize(); i++) {
            MonkeyFishingConfig.CustomLootEntry entry = items.get(i);
            try {
                ItemStack stack = CustomItemParser.parse(entry.itemSpec, registries);
                container.setItem(i, stack);
            } catch (CommandSyntaxException e) {
                MonkeyFishingMod.LOGGER.warn("Failed to parse custom {} item spec '{}': {}", mode.name().toLowerCase(), entry.itemSpec, e.getMessage());
            }
        }
        return container;
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput input, Player player) {
        if (mode == Mode.REWARDS || mode == Mode.TRASH) {
            super.clicked(slotId, button, input, player);
            return;
        }
        if (!(player instanceof ServerPlayer serverPlayer) || input != ContainerInput.PICKUP) {
            return;
        }

        if (mode == Mode.HUB) {
            switch (slotId) {
                case 11 -> open(serverPlayer, Mode.SETTINGS);
                case 13 -> open(serverPlayer, Mode.REWARDS);
                case 15 -> open(serverPlayer, Mode.TRASH);
                default -> {
                }
            }
            return;
        }

        if (slotId == 0) {
            open(serverPlayer, Mode.HUB);
            return;
        }
        adjustSetting(slotId);
    }

    private void adjustSetting(int slotId) {
        switch (slotId) {
            case 3 -> ConfigManager.CONFIG.treasureChancePercent = Math.max(1, ConfigManager.CONFIG.treasureChancePercent - 5);
            case 5 -> ConfigManager.CONFIG.treasureChancePercent = Math.min(100, ConfigManager.CONFIG.treasureChancePercent + 5);
            case 12 -> ConfigManager.CONFIG.greenZoneSizePercent = Math.max(5, ConfigManager.CONFIG.greenZoneSizePercent - 1);
            case 14 -> ConfigManager.CONFIG.greenZoneSizePercent = Math.min(50, ConfigManager.CONFIG.greenZoneSizePercent + 1);
            case 21 -> ConfigManager.CONFIG.yellowZoneSizePercent = Math.max(0, ConfigManager.CONFIG.yellowZoneSizePercent - 1);
            case 23 -> ConfigManager.CONFIG.yellowZoneSizePercent = Math.min(25, ConfigManager.CONFIG.yellowZoneSizePercent + 1);
            default -> {
                return;
            }
        }
        ConfigManager.save();
        updateSettingButtons();
        broadcastChanges();
    }

    private void updateSettingButtons() {
        setSettingButton(container, 4, Items.FISHING_ROD, "Treasure chance", ConfigManager.CONFIG.treasureChancePercent, "%");
        setSettingButton(container, 13, Items.SLIME_BALL, "Green zone size", ConfigManager.CONFIG.greenZoneSizePercent, "%");
        setSettingButton(container, 22, Items.GOLD_NUGGET, "Yellow zone per side", ConfigManager.CONFIG.yellowZoneSizePercent, "%");
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player instanceof ServerPlayer && (this.mode == Mode.REWARDS || this.mode == Mode.TRASH)) {
            List<MonkeyFishingConfig.CustomLootEntry> updated = new ArrayList<>();
            HolderLookup.Provider registries = player.level().registryAccess();
            for (int i = 0; i < SLOT_COUNT; i++) {
                ItemStack stack = this.container.getItem(i);
                if (!stack.isEmpty()) {
                    updated.add(new MonkeyFishingConfig.CustomLootEntry(CustomItemParser.toSpec(stack, registries)));
                }
            }
            if (this.mode == Mode.TRASH) {
                ConfigManager.CONFIG.trashItems = updated;
            } else {
                ConfigManager.CONFIG.customItems = updated;
            }
            ConfigManager.save();
        }
    }
}

