package com.igy.nuke3d.network;

import com.igy.nuke3d.Nuke3D;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "1";
    private static int id;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Nuke3D.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private ModNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(
                id++,
                NukeVisualPacket.class,
                NukeVisualPacket::encode,
                NukeVisualPacket::decode,
                NukeVisualPacket::handle
        );
    }
}
