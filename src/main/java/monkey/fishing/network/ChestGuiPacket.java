package monkey.fishing.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

private static int openChest(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
    ServerPlayer player = ctx.getSource().getPlayerOrException();
    
    player.openMenu(new net.minecraft.world.SimpleMenuProvider(
        (containerId, playerInventory, serverPlayer) -> net.minecraft.world.inventory.ChestMenu.threeRows(containerId, playerInventory),
        Component.literal("Monkey Fishing Chest")
    ));
    
    ctx.getSource().sendSuccess(() -> Component.literal("Opening chest GUI..."), false);
    return 1;
}
