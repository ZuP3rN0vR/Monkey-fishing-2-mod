package monkey.fishing.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class ChestGuiPacket implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("monkey-fishing-mod", "open_chest");
    public static final Type<ChestGuiPacket> TYPE = new Type<>(ID);
    
    public static void registerPayloadType() {
        PayloadTypeRegistry.clientboundPlay().register(TYPE, ChestGuiPacket.STREAM_CODEC);
    }
    
    public static void sendOpenChestGuiPacket(ServerPlayer player) {
        ServerPlayNetworking.send(player, new ChestGuiPacket());
    }
    
    public static final StreamCodec<RegistryFriendlyByteBuf, ChestGuiPacket> STREAM_CODEC = new StreamCodec<RegistryFriendlyByteBuf, ChestGuiPacket>() {
        @Override
        public ChestGuiPacket decode(RegistryFriendlyByteBuf buf) {
            return new ChestGuiPacket();
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ChestGuiPacket packet) {
            // No data to encode for this packet
        }
    };
    
    @Override
    public Type<ChestGuiPacket> type() {
        return TYPE;
    }
}
