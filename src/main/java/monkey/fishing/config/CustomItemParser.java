package monkey.fishing.config;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import monkey.fishing.MonkeyFishingMod;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Parses /give-command-style item spec strings (e.g. "minecraft:diamond[minecraft:custom_name='...']"),
 * or (for items captured straight from the chest GUI) raw item NBT produced by {@link #toSpec}.
 */
public final class CustomItemParser {
	private static final SimpleCommandExceptionType ERROR_INVALID_NBT =
			new SimpleCommandExceptionType(Component.literal("Invalid custom item data"));

	private CustomItemParser() {
	}

	public static ItemStack parse(String spec, HolderLookup.Provider registries) throws CommandSyntaxException {
		String trimmed = spec.trim();
		if (trimmed.startsWith("{")) {
			return parseNbt(trimmed, registries);
		}
		ItemParser parser = new ItemParser(registries);
		ItemInput input = parser.parse(new StringReader(spec));
		return input.createItemStack(1);
	}

	/** Serializes a full item stack (id + components) into a string that {@link #parse} can read back exactly. */
	public static String toSpec(ItemStack stack, HolderLookup.Provider registries) {
		return ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack)
				.getOrThrow(IllegalStateException::new)
				.toString();
	}

	private static ItemStack parseNbt(String nbt, HolderLookup.Provider registries) throws CommandSyntaxException {
		CompoundTag tag = TagParser.parseCompoundFully(nbt);
		return ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag)
				.resultOrPartial(MonkeyFishingMod.LOGGER::error)
				.orElseThrow(ERROR_INVALID_NBT::create);
	}
}

