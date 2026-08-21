package monkey.fishing.client;

import net.fabricmc.api.ClientModInitializer;

public class MonkeyFishingModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HudRenderCallback.EVENT.register(new FishingHudOverlay());
	}
}