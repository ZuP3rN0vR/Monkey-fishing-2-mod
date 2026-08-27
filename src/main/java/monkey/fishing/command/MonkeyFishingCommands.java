package monkey.fishing.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import me.lucko.fabric.api.permissions.v0.Permissions;
import monkey.fishing.config.ConfigManager;
import monkey.fishing.config.CustomItemParser;
import monkey.fishing.config.MonkeyFishingConfig;
import monkey.fishing.network.ChestGuiPacket;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Server commands for editing the treasure config; gated behind the monkeyfishing.config.edit permission. */
public final class MonkeyFishingCommands {
	private static final String PERMISSION = "monkeyfishing.config.edit";

	private MonkeyFishingCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(
				Commands.literal("monkeyfishing")
						.requires(Permissions.require(PERMISSION, 4))
						.then(Commands.literal("reload").executes(MonkeyFishingCommands::reload))
						.then(Commands.literal("treasurechance")
								.then(Commands.literal("get").executes(MonkeyFishingCommands::getChance))
								.then(Commands.argument("percent", IntegerArgumentType.integer(1, 100))
										.executes(MonkeyFishingCommands::setChance)))
						.then(Commands.literal("item")
								.then(Commands.literal("list").executes(MonkeyFishingCommands::listItems))
								.then(Commands.literal("add")
										.then(Commands.argument("spec", StringArgumentType.greedyString())
												.executes(MonkeyFishingCommands::addItem)))
								.then(Commands.literal("remove")
										.then(Commands.argument("index", IntegerArgumentType.integer(0))
												.executes(MonkeyFishingCommands::removeItem))))
						.then(Commands.literal("chest")
								.executes(MonkeyFishingCommands::openChest)))); // Linked your method here
	}

	private static int reload(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
		ConfigManager.reload();
		ctx.getSource().sendSuccess(() -> Component.literal("Monkey Fishing config reloaded from disk."), true);
		return 1;
	}

	private static int getChance(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
		ctx.getSource().sendSuccess(() -> Component.literal(
				"Treasure chance on green: " + ConfigManager.CONFIG.treasureChancePercent + "%"), false);
		return 1;
	}

	private static int setChance(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
		int percent = IntegerArgumentType.getInteger(ctx, "percent");
		ConfigManager.CONFIG.treasureChancePercent = percent;
		ConfigManager.save();
		ctx.getSource().sendSuccess(() -> Component.literal("Treasure chance on green set to " + percent + "%"), true);
		return 1;
	}

	private static int listItems(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
		List<MonkeyFishingConfig.CustomLootEntry> items = ConfigManager.CONFIG.customItems;
		if (items.isEmpty()) {
			ctx.getSource().sendSuccess(() -> Component.literal("No custom treasure items configured."), false);
			return 1;
		}
		for (int i = 0; i < items.size(); i++) {
			MonkeyFishingConfig.CustomLootEntry entry = items.get(i);
			int index = i;
			ctx.getSource().sendSuccess(() -> Component.literal(
					index + ": " + entry.itemSpec + " (weight " + entry.weight + ")"), false);
		}
		return items.size();
	}

	private static int addItem(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		String spec = StringArgumentType.getString(ctx, "spec");
		ItemStack parsed = CustomItemParser.parse(spec, ctx.getSource().getServer().reloadableRegistries().lookup());
		if (parsed.isEmpty()) {
			ctx.getSource().sendFailure(Component.literal("That item spec resolved to an empty item."));
			return 0;
		}
		ConfigManager.CONFIG.customItems.add(new MonkeyFishingConfig.CustomLootEntry(spec));
		ConfigManager.save();
		ctx.getSource().sendSuccess(() -> Component.literal("Added custom treasure item: " + spec), true);
		return 1;
	}

	private static int removeItem(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) {
		int index = IntegerArgumentType.getInteger(ctx, "index");
		List<MonkeyFishingConfig.CustomLootEntry> items = ConfigManager.CONFIG.customItems;
		if (index < 0 || index >= items.size()) {
			ctx.getSource().sendFailure(Component.literal("No custom item at index " + index));
			return 0;
		}
		MonkeyFishingConfig.CustomLootEntry removed = items.remove(index);
		ConfigManager.save();
		ctx.getSource().sendSuccess(() -> Component.literal("Removed custom treasure item: " + removed.itemSpec), true);
		return 1;
	}

	private static int openChest(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = (ServerPlayer) ctx.getSource().getPlayerOrException();
		ChestGuiPacket.sendOpenChestGuiPacket(player);
		ctx.getSource().sendSuccess(() -> Component.literal("Opening chest GUI..."), false);
		return 1;
	}
}
