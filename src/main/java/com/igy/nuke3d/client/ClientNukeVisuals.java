package com.igy.nuke3d.client;

import com.igy.nuke3d.Nuke3D;
import com.igy.nuke3d.network.NukeVisualPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Nuke3D.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientNukeVisuals {
    private static final Map<UUID, FX> ACTIVE = new LinkedHashMap<>();

    private ClientNukeVisuals() {}

    public static void accept(NukeVisualPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || !mc.level.dimension().location().toString().equals(packet.dimension())) return;

        if (packet.mode() == NukeVisualPacket.START) {
            ACTIVE.put(packet.eventId(), new FX(packet));
        } else if (packet.mode() == NukeVisualPacket.STOP) {
            ACTIVE.remove(packet.eventId());
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.phase != TickEvent.Phase.END || mc.level == null || mc.isPaused() || ACTIVE.isEmpty()) return;
        Iterator<FX> iterator = ACTIVE.values().iterator();
        while (iterator.hasNext()) {
            FX fx = iterator.next();
            fx.t++;
            if (fx.t > fx.duration + 40) iterator.remove();
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || ACTIVE.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Camera camera = event.getCamera();
        PoseStack pose = event.getPoseStack();
        float partial = event.getPartialTick();

        for (FX fx : ACTIVE.values()) {
            double dx = fx.x - camera.getPosition().x;
            double dy = fx.y - camera.getPosition().y;
            double dz = fx.z - camera.getPosition().z;
            double maxDistance = 700.0;
            if (dx * dx + dy * dy + dz * dz > maxDistance * maxDistance) continue;

            pose.pushPose();
            pose.translate(dx, dy, dz);
            float t = fx.t + partial;
            NukeVisuals.renderNuke(pose, t, fx.duration, fx.damageRadius, fx.terrainRadius, fx.phase, fx.seed);
            pose.popPose();
        }
        VisualMesh.restoreState();
    }

    @SubscribeEvent
    public static void shake(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || ACTIVE.isEmpty()) return;
        float partial = (float) event.getPartialTick();

        for (FX fx : ACTIVE.values()) {
            float t = fx.t + partial;
            double distSq = mc.player.distanceToSqr(fx.x, fx.y, fx.z);
            double range = Math.max(38.0, fx.damageRadius * 2.6);
            if (distSq > range * range) continue;
            float distanceFactor = (float) Math.max(0.0, 1.0 - Math.sqrt(distSq) / range);
            float power = impactPulse(t, Math.max(16, fx.duration * 0.43f), 36.0f, 1.32f) * distanceFactor;
            if (power <= 0.001f) continue;
            event.setYaw(event.getYaw() + Mth.sin(t * 1.73f + fx.phase) * 0.62f * power);
            event.setPitch(event.getPitch() + Mth.cos(t * 1.31f + fx.phase * 0.7f) * 0.50f * power);
            event.setRoll(event.getRoll() + Mth.sin(t * 0.97f + fx.phase * 1.3f) * 0.84f * power);
        }
    }

    private static float impactPulse(float t, float center, float width, float strength) {
        float d = Math.abs(t - center);
        return d >= width ? 0.0f : (1.0f - d / width) * strength;
    }

    private static final class FX {
        final UUID id;
        final double x;
        final double y;
        final double z;
        final int duration;
        final double damageRadius;
        final int terrainRadius;
        final long seed;
        final float phase;
        int t;

        FX(NukeVisualPacket packet) {
            this.id = packet.eventId();
            this.x = packet.x();
            this.y = packet.y();
            this.z = packet.z();
            this.duration = Math.max(1, packet.duration());
            this.damageRadius = Math.max(1.0, packet.damageRadius());
            this.terrainRadius = Math.max(0, packet.terrainRadius());
            this.seed = packet.seed();
            this.phase = (float) ((seed ^ (seed >>> 32)) & 0xFFFF) / 65535.0f * Mth.PI * 2.0f;
        }
    }
}
