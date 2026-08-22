package monkey.fishing.config;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;

/** Parses /give-command-style item spec strings (e.g. "minecraft:diamond[minecraft:custom_name='...']"). */
public final class CustomItemParser {
	private CustomItemParser() {
	}

	public static ItemStack parse(String spec, HolderLookup.Provider registries) throws CommandSyntaxException {
		ItemParser parser = new ItemParser(registries);
		ItemInput input = parser.parse(new StringReader(spec));
		return input.createItemStack(1);
	}
}
