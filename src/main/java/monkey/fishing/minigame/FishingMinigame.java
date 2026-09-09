package monkey.fishing.minigame;

import monkey.fishing.config.ConfigManager;
import net.minecraft.world.entity.projectile.FishingHook;

/**
 * Deterministic bead/green-zone math shared by client (rendering) and server (reel-in check).
 * Everything is derived from the hook's UUID (seed) and its tickCount (already ticked
 * identically on both logical sides), so no extra networking is required to keep them in sync.
 */
public final class FishingMinigame {
	private static final float DEFAULT_GREEN_ZONE_LENGTH = 0.18f;
	private static final float DEFAULT_YELLOW_ZONE_PADDING = 0.08f;

	private FishingMinigame() {
	}

	private static long seedFor(FishingHook hook) {
		return hook.getUUID().getLeastSignificantBits();
	}

	/** Bead position on the bar, from 0 (bottom) to 1 (top). */
	public static float beadPosition(FishingHook hook) {
		long seed = seedFor(hook);
		float periodTicks = 30f + Math.floorMod(seed, 20L);
		float phase = (hook.tickCount % periodTicks) / periodTicks;
		return (float) (0.5 - 0.5 * Math.cos(phase * Math.PI * 2));
	}

	/** Start of the green zone on the bar, clamped so the zone (plus any larger configured length) can't overflow the top. */
	public static float greenZoneStart(FishingHook hook) {
		long seed = seedFor(hook) / 97L;
		return Math.min(0.1f + Math.floorMod(seed, 60L) / 100f, 1f - greenZoneLength());
	}

	public static float greenZoneLength() {
		if (ConfigManager.CONFIG == null) {
			return DEFAULT_GREEN_ZONE_LENGTH;
		}
		return Math.clamp(ConfigManager.CONFIG.greenZoneSizePercent / 100f, 0.05f, 0.5f);
	}

	/** Thickness of each yellow band flanking the green zone. */
	public static float yellowZonePadding() {
		if (ConfigManager.CONFIG == null) {
			return DEFAULT_YELLOW_ZONE_PADDING;
		}
		return Math.clamp(ConfigManager.CONFIG.yellowZoneSizePercent / 100f, 0f, 0.25f);
	}

	/** Bottom of the lower yellow band (clamped to the bar). */
	public static float yellowZoneStart(FishingHook hook) {
		return Math.max(0f, greenZoneStart(hook) - yellowZonePadding());
	}

	/** Top of the upper yellow band (clamped to the bar). */
	public static float yellowZoneEnd(FishingHook hook) {
		return Math.min(1f, greenZoneStart(hook) + greenZoneLength() + yellowZonePadding());
	}

	public static boolean isBeadInGreenZone(FishingHook hook) {
		float bead = beadPosition(hook);
		float start = greenZoneStart(hook);
		return bead >= start && bead <= start + greenZoneLength();
	}

	/** Bead is in the yellow margin around the green zone (yellow never overlaps green). */
	public static boolean isBeadInYellowZone(FishingHook hook) {
		float bead = beadPosition(hook);
		return bead >= yellowZoneStart(hook) && bead <= yellowZoneEnd(hook) && !isBeadInGreenZone(hook);
	}
}
