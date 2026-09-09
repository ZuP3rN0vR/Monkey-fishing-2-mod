package monkey.fishing.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Set;
import monkey.fishing.MonkeyFishingMod;
import monkey.fishing.config.ConfigManager;
import monkey.fishing.config.CustomItemParser;
import monkey.fishing.config.MonkeyFishingConfig;
import monkey.fishing.minigame.FishingMinigame;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FishingHook.class)
public class FishingHookMixin {
	// Items obtainable from the vanilla gameplay/fishing/treasure loot table.
	private static final Set<Item> TREASURE_ITEMS = Set.of(
			Items.ENCHANTED_BOOK, Items.NAME_TAG, Items.NAUTILUS_SHELL, Items.SADDLE, Items.BOW, Items.FISHING_ROD);

	@Redirect(method = "retrieve", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"))
	private ObjectArrayList<ItemStack> monkeyfishing$maybeBoostTreasure(LootTable lootTable, LootParams params) {
		ObjectArrayList<ItemStack> items = lootTable.getRandomItems(params);
		FishingHook hook = (FishingHook) (Object) this;

		float chance = ConfigManager.CONFIG.treasureChancePercent / 100f;
		boolean alreadyTreasure = items.stream().anyMatch(stack -> TREASURE_ITEMS.contains(stack.getItem()));
		if (!alreadyTreasure && FishingMinigame.isBeadInGreenZone(hook) && hook.getRandom().nextFloat() < chance) {
			ItemStack custom = monkeyfishing$rollCustomItem(hook, ConfigManager.CONFIG.customItems);
			if (custom != null) {
				ObjectArrayList<ItemStack> result = new ObjectArrayList<>();
				result.add(custom);
				return result;
			}

			LootTable treasureTable = params.getLevel().getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING_TREASURE);
			return treasureTable.getRandomItems(params);
		}

		// Bead in the yellow margin: guaranteed trash, replacing whatever vanilla rolled.
		if (FishingMinigame.isBeadInYellowZone(hook)) {
			ItemStack trash = monkeyfishing$rollCustomItem(hook, ConfigManager.CONFIG.trashItems);
			if (trash != null) {
				ObjectArrayList<ItemStack> result = new ObjectArrayList<>();
				result.add(trash);
				return result;
			}

			LootTable junkTable = params.getLevel().getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING_JUNK);
			return junkTable.getRandomItems(params);
		}

		return items;
	}

	/** Picks a weighted-random item from the given config list, or null if empty / all fail to parse. */
	private static ItemStack monkeyfishing$rollCustomItem(FishingHook hook, List<MonkeyFishingConfig.CustomLootEntry> entries) {
		if (entries.isEmpty()) {
			return null;
		}

		int totalWeight = entries.stream().mapToInt(e -> Math.max(1, e.weight)).sum();
		int roll = hook.getRandom().nextInt(totalWeight);
		int cumulative = 0;
		for (MonkeyFishingConfig.CustomLootEntry entry : entries) {
			cumulative += Math.max(1, entry.weight);
			if (roll < cumulative) {
				try {
					return CustomItemParser.parse(entry.itemSpec, hook.level().getServer().reloadableRegistries().lookup());
				} catch (Exception e) {
					MonkeyFishingMod.LOGGER.warn("Failed to parse custom item spec '{}': {}", entry.itemSpec, e.getMessage());
					return null;
				}
			}
		}
		return null;
	}
}

