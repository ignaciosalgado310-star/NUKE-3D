package com.igy.nuke3d.disaster;

import com.igy.nuke3d.NukeTimeline;
import com.igy.nuke3d.config.NukeConfig;
import com.igy.nuke3d.sound.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/** NUKE gameplay timeline with PURPURE-style targeted totem consumption. */
public final class ActiveNuke {
    private static final int PURPURE_TOTEM_INTERVAL = 2;
    private static final int END_PADDING_TICKS = 25;
    private static final int AFTERMATH_NOT_STARTED = -2;
    private static final int CRATER_SCAN_BURST = 1_000_000;
    private static final int CRATER_CHANGE_BURST = 500_000;
    private static final int AFTERMATH_SCAN_BURST = 50_000;
    private static final int AFTERMATH_CHANGE_BURST = 25_000;

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
    private int aftermathCursor = AFTERMATH_NOT_STARTED;

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

    /** The cinematic duration is locked to the 17.92-second replacement audio. */
    public int visualDuration() {
        return NukeTimeline.SEQUENCE_TICKS;
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

        boolean terrainDone = craterCursor < 0 && aftermathCursor < 0 && aftermathCursor != AFTERMATH_NOT_STARTED;
        return age >= effectiveDuration && terrainDone;
    }

    private void tickNuke(int duration) {
        int impact = impactTick(duration);
        int craterRadius = effectiveTerrainRadius();

        // One continuous soundtrack from the first falling shot through the final explosion frame.
        // All old auxiliary explosion sounds were removed so they cannot cover the replacement audio.
        if (age == 0) {
            NukeEffects.sound(level, center, ModSounds.NUKE_SEQUENCE.get(), 8.0F, 1.0F);
        }

        if (age >= impact && craterCursor >= 0) {
            if (NukeConfig.ALLOW_TERRAIN_DAMAGE.get()) {
                craterCursor = NukeEffects.carveNuclearCrater(
                        level, center, craterRadius, seed, craterCursor,
                        CRATER_SCAN_BURST, CRATER_CHANGE_BURST
                );
            } else {
                craterCursor = -1;
            }
        }

        if (craterCursor < 0 && aftermathCursor == AFTERMATH_NOT_STARTED) {
            aftermathCursor = 0;
        }

        if (aftermathCursor >= 0) {
            if (!NukeConfig.ALLOW_TERRAIN_DAMAGE.get()) {
                aftermathCursor = -1;
            } else {
                aftermathCursor = NukeEffects.decorateAftermath(
                        level, center, craterRadius, seed, aftermathCursor,
                        AFTERMATH_SCAN_BURST, AFTERMATH_CHANGE_BURST
                );
            }
        }

        pulseDamage(impact);
    }

    private int impactTick(int ignoredDuration) {
        return NukeTimeline.IMPACT_TICK;
    }

    private int effectiveTerrainRadius() {
        int configured = NukeConfig.TERRAIN_RADIUS.get();
        return Math.max(42, (int) Math.round(configured * 2.5));
    }

    private int pulseLimit() {
        return pulseOverride != null ? Math.max(0, pulseOverride) : NukeConfig.DAMAGE_PULSES.get();
    }

    private int pulseInterval() {
        return targetPlayerId != null ? PURPURE_TOTEM_INTERVAL : Math.max(1, NukeConfig.PULSE_INTERVAL_TICKS.get());
    }

    private int effectiveDuration(int baseDuration) {
        int minimum = Math.max(baseDuration, NukeTimeline.SEQUENCE_TICKS);
        int pulses = pulseLimit();
        if (pulses <= 0) return minimum;
        int start = NukeTimeline.IMPACT_TICK;
        long needed = (long) start
                + (long) (pulses - 1) * pulseInterval()
                + END_PADDING_TICKS;
        return (int) Math.min(Integer.MAX_VALUE - 1024L, Math.max(minimum, needed));
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
