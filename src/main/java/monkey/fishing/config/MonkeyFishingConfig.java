package monkey.fishing.config;

import java.util.ArrayList;
import java.util.List;

/** Plain data holder persisted to config/monkey-fishing-mod.json via Gson. */
public class MonkeyFishingConfig {
	/** 1-100, chance to reroll a catch into treasure when reeled in on the green zone. */
	public int treasureChancePercent = 30;

	public List<CustomLootEntry> customItems = new ArrayList<>();

	public static class CustomLootEntry {
		/** A /give-command-style item spec, e.g. "minecraft:diamond" or "minecraft:diamond[minecraft:custom_name='...']". */
		public String itemSpec;
		/** Relative weight when multiple custom items are configured. */
		public int weight = 1;

		public CustomLootEntry() {
		}

		public CustomLootEntry(String itemSpec) {
			this.itemSpec = itemSpec;
		}

		public CustomLootEntry(String itemSpec, int weight) {
			this.itemSpec = itemSpec;
			this.weight = weight;
		}
	}
}
