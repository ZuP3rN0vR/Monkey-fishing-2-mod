package monkey.fishing.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import monkey.fishing.MonkeyFishingMod;
import net.fabricmc.loader.api.FabricLoader;

/** Loads/saves the mod config to config/monkey-fishing-mod.json. */
public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("monkey-fishing-mod.json");

	public static MonkeyFishingConfig CONFIG = load();

	private ConfigManager() {
	}

	public static MonkeyFishingConfig load() {
		if (Files.exists(PATH)) {
			try {
				String json = Files.readString(PATH, StandardCharsets.UTF_8);
				MonkeyFishingConfig loaded = GSON.fromJson(json, MonkeyFishingConfig.class);
				if (loaded != null) {
					return loaded;
				}
			} catch (IOException e) {
				MonkeyFishingMod.LOGGER.error("Failed to read monkey-fishing-mod.json, using defaults", e);
			}
		}
		MonkeyFishingConfig defaults = new MonkeyFishingConfig();
		CONFIG = defaults;
		save();
		return defaults;
	}

	public static void reload() {
		CONFIG = load();
	}

	public static void save() {
		try {
			Files.createDirectories(PATH.getParent());
			Files.writeString(PATH, GSON.toJson(CONFIG), StandardCharsets.UTF_8);
		} catch (IOException e) {
			MonkeyFishingMod.LOGGER.error("Failed to save monkey-fishing-mod.json", e);
		}
	}
}
