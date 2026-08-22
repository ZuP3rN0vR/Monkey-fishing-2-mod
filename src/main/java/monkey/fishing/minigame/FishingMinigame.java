package monkey.fishing.minigame;

import net.minecraft.world.entity.projectile.FishingHook;

/**
 * Deterministic bead/green-zone math shared by client (rendering) and server (reel-in check).
 * Everything is derived from the hook's UUID (seed) and its tickCount (already ticked
 * identically on both logical sides), so no extra networking is required to keep them in sync.
 */
public final class FishingMinigame {
	/** Chance to reroll a non-treasure catch into a treasure catch when reeled in on green. */
	public static final float REROLL_CHANCE_ON_GREEN = 0.30f;

	private static final float GREEN_ZONE_LENGTH = 0.18f;

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

	/** Start of the green zone on the bar, from 0 to (1 - GREEN_ZONE_LENGTH). */
	public static float greenZoneStart(FishingHook hook) {
		long seed = seedFor(hook) / 97L;
		return 0.1f + Math.floorMod(seed, 60L) / 100f;
	}

	public static float greenZoneLength() {
		return GREEN_ZONE_LENGTH;
	}

	public static boolean isBeadInGreenZone(FishingHook hook) {
		float bead = beadPosition(hook);
		float start = greenZoneStart(hook);
		return bead >= start && bead <= start + GREEN_ZONE_LENGTH;
	}
}
