package monkey.fishing.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Set;
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

		boolean alreadyTreasure = items.stream().anyMatch(stack -> TREASURE_ITEMS.contains(stack.getItem()));
		if (!alreadyTreasure && FishingMinigame.isBeadInGreenZone(hook)
				&& hook.getRandom().nextFloat() < FishingMinigame.REROLL_CHANCE_ON_GREEN) {
			LootTable treasureTable = params.getLevel().getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING_TREASURE);
			return treasureTable.getRandomItems(params);
		}

		return items;
	}
}
