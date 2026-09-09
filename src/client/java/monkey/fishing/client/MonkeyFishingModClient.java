package monkey.fishing.client;

import monkey.fishing.menu.ChestGuiMenu;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class MonkeyFishingModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FishingHudOverlay.register();
		MenuScreens.register(ChestGuiMenu.TYPE, ChestGui::new);
	}
}