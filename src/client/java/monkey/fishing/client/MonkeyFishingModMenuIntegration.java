package monkey.fishing.client;

import java.util.ArrayList;
import java.util.List;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import monkey.fishing.config.ConfigManager;
import monkey.fishing.config.MonkeyFishingConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Client-only config screen shown by Mod Menu. Edits the same on-disk config used by the server,
 * so it's mainly useful for singleplayer or a server owner editing their own local files; players
 * connected to a remote server should use the /monkeyfishing commands instead (permission-gated).
 */
public final class MonkeyFishingModMenuIntegration implements com.terraformersmc.modmenu.api.ModMenuApi {
	@Override
	public com.terraformersmc.modmenu.api.ConfigScreenFactory<?> getModConfigScreenFactory() {
		return MonkeyFishingModMenuIntegration::buildScreen;
	}

	private static Screen buildScreen(Screen parent) {
		MonkeyFishingConfig config = ConfigManager.CONFIG;

		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.literal("Monkey Fishing Mod"))
				.setSavingRunnable(ConfigManager::save);

		ConfigEntryBuilder entryBuilder = builder.entryBuilder();
		ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));

		general.addEntry(entryBuilder.startIntSlider(
						Component.literal("Treasure chance on green (%)"), config.treasureChancePercent, 1, 100)
				.setDefaultValue(30)
				.setSaveConsumer(value -> config.treasureChancePercent = value)
				.build());

		List<String> currentSpecs = config.customItems.stream().map(e -> e.itemSpec).toList();
		general.addEntry(entryBuilder.startStrList(
						Component.literal("Custom treasure items (/give-style item spec per line)"), currentSpecs)
				.setDefaultValue(new ArrayList<>())
				.setSaveConsumer(specs -> {
					config.customItems.clear();
					for (String spec : specs) {
						if (spec != null && !spec.isBlank()) {
							config.customItems.add(new MonkeyFishingConfig.CustomLootEntry(spec.trim()));
						}
					}
				})
				.build());

		return builder.build();
	}
}
