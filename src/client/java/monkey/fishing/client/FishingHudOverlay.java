package monkey.fishing.client;

import monkey.fishing.MonkeyFishingMod;
import monkey.fishing.client.mixin.FishingHookBitingAccessor;
import monkey.fishing.minigame.FishingMinigame;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.projectile.FishingHook;

public final class FishingHudOverlay {
	private static final int BAR_X = 20;
	private static final int BAR_WIDTH = 8;
	private static final int BAR_HEIGHT = 120;
	private static final int BEAD_HEIGHT = 6;

	private static final int COLOR_BACKGROUND = 0xAA000000;
	private static final int COLOR_BORDER = 0xFFFFFFFF;
	private static final int COLOR_GREEN_ZONE = 0xFF33CC33;
	private static final int COLOR_BEAD = 0xFFFFDD33;

	private FishingHudOverlay() {
	}

	public static void register() {
		HudElementRegistry.addLast(MonkeyFishingMod.id("fishing_bar"), FishingHudOverlay::render);
	}

	private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft client = Minecraft.getInstance();
		LocalPlayer player = client.player;
		if (player == null) {
			return;
		}

		FishingHook hook = player.fishing;
		if (hook == null || hook.isRemoved() || !((FishingHookBitingAccessor) hook).monkeyfishing$isBiting()) {
			return;
		}

		int barY = (client.getWindow().getGuiScaledHeight() - BAR_HEIGHT) / 2;
		int barX2 = BAR_X + BAR_WIDTH;
		int barY2 = barY + BAR_HEIGHT;

		graphics.fill(BAR_X - 1, barY - 1, barX2 + 1, barY2 + 1, COLOR_BORDER);
		graphics.fill(BAR_X, barY, barX2, barY2, COLOR_BACKGROUND);

		float greenStart = FishingMinigame.greenZoneStart(hook);
		float greenEnd = greenStart + FishingMinigame.greenZoneLength();
		int greenY1 = barY2 - Math.round(greenEnd * BAR_HEIGHT);
		int greenY2 = barY2 - Math.round(greenStart * BAR_HEIGHT);
		graphics.fill(BAR_X, greenY1, barX2, greenY2, COLOR_GREEN_ZONE);

		float bead = FishingMinigame.beadPosition(hook);
		int beadCenterY = barY2 - Math.round(bead * BAR_HEIGHT);
		graphics.fill(BAR_X, beadCenterY - BEAD_HEIGHT / 2, barX2, beadCenterY + BEAD_HEIGHT / 2, COLOR_BEAD);
	}
}
