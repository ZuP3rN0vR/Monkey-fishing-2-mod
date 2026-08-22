package monkey.fishing.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record ChestGuiPacket() implements CustomPacketPayload {
    public static final Type<ChestGuiPacket> TYPE = new Type<>(
        Identifier.fromNamespaceAndPath("monkey-fishing-mod", "open_chest_gui")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ChestGuiPacket> CODEC = StreamCodec.unit(new ChestGuiPacket());
    
    public static void sendOpenChestGuiPacket(ServerPlayer player) {
        ServerPlayNetworking.send(player, new ChestGuiPacket());
    }
    
    public static void registerPayloadType() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}