package com.igy.nuke3d.network;

import com.igy.nuke3d.client.ClientNukeVisuals;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record NukeVisualPacket(
        byte mode,
        UUID eventId,
        String dimension,
        double x,
        double y,
        double z,
        int duration,
        double damageRadius,
        int terrainRadius,
        long seed
) {
    public static final byte START = 0;
    public static final byte STOP = 1;

    public static void encode(NukeVisualPacket message, FriendlyByteBuf buffer) {
        buffer.writeByte(message.mode);
        buffer.writeUUID(message.eventId);
        buffer.writeUtf(message.dimension, 128);
        buffer.writeDouble(message.x);
        buffer.writeDouble(message.y);
        buffer.writeDouble(message.z);
        buffer.writeVarInt(message.duration);
        buffer.writeDouble(message.damageRadius);
        buffer.writeVarInt(message.terrainRadius);
        buffer.writeLong(message.seed);
    }

    public static NukeVisualPacket decode(FriendlyByteBuf buffer) {
        return new NukeVisualPacket(
                buffer.readByte(),
                buffer.readUUID(),
                buffer.readUtf(128),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readVarInt(),
                buffer.readDouble(),
                buffer.readVarInt(),
                buffer.readLong()
        );
    }

    public static void handle(NukeVisualPacket message, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientNukeVisuals.accept(message)
        ));
        context.setPacketHandled(true);
    }
}
