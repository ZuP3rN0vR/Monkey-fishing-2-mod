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

/** Real, server-synced chest menu backing the Monkey Fishing chest GUI (owns its own MenuType so it isn't drawn by vanilla's chest screen). */
public class ChestGuiMenu extends ChestMenu {
    public static final MenuType<ChestGuiMenu> TYPE = Registry.register(
        BuiltInRegistries.MENU, MonkeyFishingMod.id("chest_gui"),
        new MenuType<>(ChestGuiMenu::new, FeatureFlags.VANILLA_SET)
    );

    private static final int SLOT_COUNT = 27;

    private final Container container;

    public ChestGuiMenu(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(SLOT_COUNT));
    }

    public ChestGuiMenu(int syncId, Inventory playerInventory, Container container) {
        super(TYPE, syncId, playerInventory, container, 3);
        this.container = container;
    }

    /** Builds a fresh backing container from the configured custom loot items. */
    public static Container buildContainer(HolderLookup.Provider registries) {
        SimpleContainer container = new SimpleContainer(SLOT_COUNT);
        var items = ConfigManager.CONFIG.customItems;
        for (int i = 0; i < items.size() && i < container.getContainerSize(); i++) {
            MonkeyFishingConfig.CustomLootEntry entry = items.get(i);
            try {
                ItemStack stack = CustomItemParser.parse(entry.itemSpec, registries);
                container.setItem(i, stack);
            } catch (CommandSyntaxException e) {
                MonkeyFishingMod.LOGGER.warn("Failed to parse custom treasure item spec '{}': {}", entry.itemSpec, e.getMessage());
            }
        }
        return container;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // Persist whatever ended up in the top slots back into the treasure pool config.
        if (player instanceof ServerPlayer) {
            List<MonkeyFishingConfig.CustomLootEntry> updated = new ArrayList<>();
            HolderLookup.Provider registries = player.level().registryAccess();
            for (int i = 0; i < SLOT_COUNT; i++) {
                ItemStack stack = this.container.getItem(i);
                if (!stack.isEmpty()) {
                    updated.add(new MonkeyFishingConfig.CustomLootEntry(CustomItemParser.toSpec(stack, registries)));
                }
            }
            ConfigManager.CONFIG.customItems = updated;
            ConfigManager.save();
        }
    }
}

