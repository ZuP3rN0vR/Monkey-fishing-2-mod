package monkey.fishing.menu;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayList;
import java.util.List;
import monkey.fishing.MonkeyFishingMod;
import monkey.fishing.config.ConfigManager;
import monkey.fishing.config.CustomItemParser;
import monkey.fishing.config.MonkeyFishingConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/** Real, server-synced chest menu backing the Monkey Fishing chest GUIs (owns its own MenuType so it isn't drawn by vanilla's chest screen). */
public class ChestGuiMenu extends ChestMenu {
    public static final MenuType<ChestGuiMenu> TYPE = Registry.register(
        BuiltInRegistries.MENU, MonkeyFishingMod.id("chest_gui"),
        new MenuType<>(ChestGuiMenu::new, FeatureFlags.VANILLA_SET)
    );

    private static final int SLOT_COUNT = 27;

    /** Which config list this GUI edits. Persistence only runs server-side, so the client default is harmless. */
    public enum Target {
        TREASURE, TRASH
    }

    private final Container container;
    private final Target target;

    public ChestGuiMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(SLOT_COUNT), Target.TREASURE);
    }

    public ChestGuiMenu(int syncId, Inventory playerInventory, Container container) {
        this(syncId, playerInventory, container, Target.TREASURE);
    }

    public ChestGuiMenu(int syncId, Inventory playerInventory, Container container, Target target) {
        super(TYPE, syncId, playerInventory, container, 3);
        this.container = container;
        this.target = target;
    }

    private static List<MonkeyFishingConfig.CustomLootEntry> entriesFor(Target target) {
        return target == Target.TRASH ? ConfigManager.CONFIG.trashItems : ConfigManager.CONFIG.customItems;
    }

    /** Builds a fresh backing container from the configured items for the given target. */
    public static Container buildContainer(HolderLookup.Provider registries) {
        return buildContainer(registries, Target.TREASURE);
    }

    /** Builds a fresh backing container from the configured items for the given target. */
    public static Container buildContainer(HolderLookup.Provider registries, Target target) {
        SimpleContainer container = new SimpleContainer(SLOT_COUNT);
        var items = entriesFor(target);
        for (int i = 0; i < items.size() && i < container.getContainerSize(); i++) {
            MonkeyFishingConfig.CustomLootEntry entry = items.get(i);
            try {
                ItemStack stack = CustomItemParser.parse(entry.itemSpec, registries);
                container.setItem(i, stack);
            } catch (CommandSyntaxException e) {
                MonkeyFishingMod.LOGGER.warn("Failed to parse custom {} item spec '{}': {}", target.name().toLowerCase(), entry.itemSpec, e.getMessage());
            }
        }
        return container;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // Persist whatever ended up in the top slots back into the matching loot pool config.
        if (player instanceof ServerPlayer) {
            List<MonkeyFishingConfig.CustomLootEntry> updated = new ArrayList<>();
            HolderLookup.Provider registries = player.level().registryAccess();
            for (int i = 0; i < SLOT_COUNT; i++) {
                ItemStack stack = this.container.getItem(i);
                if (!stack.isEmpty()) {
                    updated.add(new MonkeyFishingConfig.CustomLootEntry(CustomItemParser.toSpec(stack, registries)));
                }
            }
            if (this.target == Target.TRASH) {
                ConfigManager.CONFIG.trashItems = updated;
            } else {
                ConfigManager.CONFIG.customItems = updated;
            }
            ConfigManager.save();
        }
    }
}

