package monkey.fishing.client;

import monkey.fishing.network.ChestGuiPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ChestGuiClientHandler {
    private ChestGuiClientHandler() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ChestGuiPacket.TYPE, (payload, context) ->
            context.client().execute(() -> {
                var minecraft = context.client();
                if (minecraft.player != null) {
                    minecraft.setScreenAndShow(new ChestGui(minecraft.player.getInventory()));
                }
            })
        );
    }
}