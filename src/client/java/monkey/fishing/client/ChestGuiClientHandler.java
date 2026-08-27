package monkey.fishing.client;

import monkey.fishing.network.ChestGuiPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ChestMenu;
import monkey.fishing.client.ChestGui;

public final class ChestGuiClientHandler {
    private ChestGuiClientHandler() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(ChestGuiPacket.TYPE, (payload, context) ->
            context.client().execute(() -> {
                var minecraft = context.client();
                if (minecraft.player != null) {
                    var inventory = minecraft.player.getInventory();
                    var menu = ChestMenu.threeRows(0, inventory);
                    minecraft.setScreenAndShow(new ChestGui(menu, inventory, Component.literal("Monkey Fishing")));
                }
            })
        );
    }
}