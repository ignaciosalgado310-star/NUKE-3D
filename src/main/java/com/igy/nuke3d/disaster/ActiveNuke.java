package com.igy.nuke3d.disaster;

import com.igy.nuke3d.config.NukeConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/** NUKE gameplay timeline with PURPURE-style targeted totem consumption. */
public final class ActiveNuke {
    private static final int PURPURE_TOTEM_INTERVAL = 2;
    private static final int END_PADDING_TICKS = 25;
    private static final int VISUAL_HOLD_MARGIN_TICKS = 64;

    private final UUID id = UUID.randomUUID();
    private final long seed = java.util.concurrent.ThreadLocalRandom.current().nextLong();
    private final ServerLevel level;
    private final Vec3 center;
    private final Integer pulseOverride;
    private final Double damageOverrideHearts;
    private final UUID targetPlayerId;

    private int age;
    private int pulsesApplied;
    private int craterCursor;

    public ActiveNuke(ServerLevel level, Vec3 center,
                      Integer pulseOverride, Double damageOverrideHearts, UUID targetPlayerId) {
        this.level = level;
        this.center = center;
        this.pulseOverride = pulseOverride;
        this.damageOverrideHearts = damageOverrideHearts;
        this.targetPlayerId = targetPlayerId;
    }

    public UUID id() { return id; }
    public long seed() { return seed; }
    public ServerLevel level() { return level; }
    public Vec3 center() { return center; }
    public int age() { return age; }
    public int completedHits() { return pulsesApplied; }
    public int requestedHits() { return pulseLimit(); }

    public int visualDuration() {
        long duration = (long) effectiveDuration(NukeConfig.DURATION_TICKS.get()) + VISUAL_HOLD_MARGIN_TICKS;
        return (int) Math.min(Integer.MAX_VALUE - 1024L, duration);
    }

    public double visualDamageRadius() {
        return NukeConfig.DAMAGE_RADIUS.get();
    }

    public int visualTerrainRadius() {
        return effectiveTerrainRadius();
    }

    public boolean tick() {
        int duration = NukeConfig.DURATION_TICKS.get();
        int effectiveDuration = effectiveDuration(duration);

        if (targetPlayerId != null) {
            ServerPlayer target = targetPlayer();
            if (target == null || !target.isAlive() || target.serverLevel() != level) return true;
        }

        tickNuke(duration);
        age++;
        return age >= effectiveDuration;
    }

    private void tickNuke(int duration) {
        int impact = Math.max(16, (int) (duration * 0.43));
        int craterRadius = effectiveTerrainRadius();

        if (age == 0) NukeEffects.sound(level, center, SoundEvents.WITHER_SPAWN, 4.0F, 0.6F);
        if (age == impact) {
            NukeEffects.sound(level, center, SoundEvents.GENERIC_EXPLODE, 10.0F, 0.48F);
            NukeEffects.ejectBlocks(level, center, craterRadius, 44, 1.12, 1.34);
        }

        if (age >= impact && age < impact + 150 && craterCursor >= 0) {
            int configuredBudget = NukeConfig.MAX_BLOCK_CHANGES_PER_TICK.get();
            if (configuredBudget > 0) {
                // The sphere is scanned progressively. A larger dedicated crater budget lets the complete
                // rounded volume finish during the cinematic instead of leaving random vertical columns.
                int craterChanges = Math.min(3200, configuredBudget * 4);
                craterCursor = NukeEffects.carveSphericalCrater(
                        level, center, craterRadius, craterCursor, 24000, craterChanges
                );
            }

            if ((age - impact) < 52 && age % 4 == 0) {
                NukeEffects.ejectBlocks(level, center, craterRadius, 24, 0.98, 1.16);
            }
        }
        pulseDamage(impact);
    }

    private int effectiveTerrainRadius() {
        int configured = NukeConfig.TERRAIN_RADIUS.get();
        return Math.max(48, configured * 3);
    }

    private int pulseLimit() {
        return pulseOverride != null ? Math.max(0, pulseOverride) : NukeConfig.DAMAGE_PULSES.get();
    }

    private int pulseInterval() {
        return targetPlayerId != null ? PURPURE_TOTEM_INTERVAL : Math.max(1, NukeConfig.PULSE_INTERVAL_TICKS.get());
    }

    private int effectiveDuration(int baseDuration) {
        int pulses = pulseLimit();
        if (pulses <= 0) return baseDuration;
        int start = Math.max(16, (int) (baseDuration * 0.43));
        long needed = (long) start
                + (long) (pulses - 1) * pulseInterval()
                + END_PADDING_TICKS;
        return (int) Math.min(Integer.MAX_VALUE - 1024L, Math.max(baseDuration, needed));
    }

    private void pulseDamage(int startAge) {
        if (age < startAge || pulsesApplied >= pulseLimit()) return;
        int interval = pulseInterval();
        if ((age - startAge) % interval != 0) return;

        if (targetPlayerId != null) {
            ServerPlayer player = targetPlayer();
            if (player == null || !player.isAlive() || player.serverLevel() != level) return;
            purpureStyleHit(player);
        }
        pulsesApplied++;
    }

    private void purpureStyleHit(ServerPlayer player) {
        if (consumeTotem(player)) {
            level.broadcastEntityEvent(player, (byte) 35);
        } else {
            float lethal = 1000.0F;
            if (damageOverrideHearts != null) {
                lethal = (float) Math.max(lethal, damageOverrideHearts * 2.0);
            }
            player.hurt(player.damageSources().magic(), lethal);
        }
    }

    private boolean consumeTotem(ServerPlayer player) {
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(Items.TOTEM_OF_UNDYING)) {
            offhand.shrink(1);
            return true;
        }

        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.is(Items.TOTEM_OF_UNDYING)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private ServerPlayer targetPlayer() {
        if (targetPlayerId == null) return null;
        return level.getServer().getPlayerList().getPlayer(targetPlayerId);
    }
}
