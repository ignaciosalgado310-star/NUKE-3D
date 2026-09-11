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
    private static final int AFTERMATH_NOT_STARTED = -2;

    // The user explicitly prefers a hard impact hitch over watching the crater excavate for seconds.
    // These burst budgets are intentionally huge so the crater + static aftermath normally finish
    // in the same impact tick (or at worst the next couple of ticks on very dense terrain).
    private static final int CRATER_SCAN_BURST = 900_000;
    private static final int CRATER_CHANGE_BURST = 220_000;
    private static final int AFTERMATH_SCAN_BURST = 40_000;
    private static final int AFTERMATH_CHANGE_BURST = 16_000;

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

    /**
     * Visual duration deliberately follows the configured base timeline, not the totem-hit extension.
     * That keeps the client impact frame synchronized with the real server impact frame even when
     * hundreds of PURPURE-style totem hits keep the gameplay event alive longer.
     */
    public int visualDuration() {
        return Math.max(20, NukeConfig.DURATION_TICKS.get());
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

        if (age == 0) {
            NukeEffects.sound(level, center, SoundEvents.WITHER_SPAWN, 4.0F, 0.58F);
        }

        if (age == impact) {
            NukeEffects.sound(level, center, SoundEvents.GENERIC_EXPLODE, 10.0F, 0.44F);
        }
        if (age == impact + 9) {
            NukeEffects.sound(level, center, SoundEvents.GENERIC_EXPLODE, 6.5F, 0.72F);
        }

        // Hard burst: finish the destructive volume immediately instead of visibly digging layer by layer.
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

        // Decoration begins in the very same tick the crater finishes, with another large burst.
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

    private int impactTick(int duration) {
        return Math.max(16, (int) (duration * 0.43));
    }

    private int effectiveTerrainRadius() {
        int configured = NukeConfig.TERRAIN_RADIUS.get();
        // Previous rule was max(48, configured*3). This is ~15-20% smaller at the default value.
        return Math.max(42, (int) Math.round(configured * 2.5));
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
        int start = impactTick(baseDuration);
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
