package com.igy.nuke3d.disaster;

import com.igy.nuke3d.Nuke3D;
import com.igy.nuke3d.config.NukeConfig;
import com.igy.nuke3d.network.ModNetwork;
import com.igy.nuke3d.network.NukeVisualPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Nuke3D.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NukeManager {
    private static final List<ActiveNuke> ACTIVE = new ArrayList<>();

    public static boolean start(ServerLevel level, Vec3 center,
                                Integer pulseOverride, Double damageOverrideHearts, UUID targetPlayerId) {
        if (ACTIVE.size() >= NukeConfig.MAX_ACTIVE_NUKES.get()) return false;

        ActiveNuke nuke = new ActiveNuke(level, center, pulseOverride, damageOverrideHearts, targetPlayerId);
        ACTIVE.add(nuke);
        sendStart(nuke);

        if (NukeConfig.BROADCAST_START.get()) {
            String extra = pulseOverride == null ? "" : " | tótems: " + pulseOverride;
            level.getServer().getPlayerList().broadcastSystemMessage(
                    Component.literal("§5[NUKE 3D] §fNUKE §7en §f"
                            + (int) center.x + " " + (int) center.y + " " + (int) center.z + extra),
                    false
            );
        }
        return true;
    }

    public static List<ActiveNuke> snapshot() {
        return List.copyOf(ACTIVE);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ACTIVE.isEmpty()) return;

        Iterator<ActiveNuke> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            ActiveNuke nuke = iterator.next();
            try {
                if (nuke.tick()) {
                    sendStop(nuke);
                    iterator.remove();
                }
            } catch (Throwable throwable) {
                Nuke3D.LOGGER.error("Stopping NUKE after an error", throwable);
                sendStop(nuke);
                iterator.remove();
            }
        }
    }

    private static void sendStart(ActiveNuke nuke) {
        Vec3 p = nuke.center();
        ModNetwork.CHANNEL.send(
                PacketDistributor.DIMENSION.with(() -> nuke.level().dimension()),
                new NukeVisualPacket(
                        NukeVisualPacket.START,
                        nuke.id(),
                        nuke.level().dimension().location().toString(),
                        p.x, p.y, p.z,
                        nuke.visualDuration(),
                        nuke.visualDamageRadius(),
                        nuke.visualTerrainRadius(),
                        nuke.seed()
                )
        );
    }

    private static void sendStop(ActiveNuke nuke) {
        Vec3 p = nuke.center();
        ModNetwork.CHANNEL.send(
                PacketDistributor.DIMENSION.with(() -> nuke.level().dimension()),
                new NukeVisualPacket(
                        NukeVisualPacket.STOP,
                        nuke.id(),
                        nuke.level().dimension().location().toString(),
                        p.x, p.y, p.z,
                        nuke.visualDuration(),
                        nuke.visualDamageRadius(),
                        nuke.visualTerrainRadius(),
                        nuke.seed()
                )
        );
    }

    private NukeManager() {}
}
