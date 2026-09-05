package com.igy.nuke3d.disaster;

import com.igy.nuke3d.config.NukeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/** Server-side NUKE terrain helpers. No Minecraft particles are used. */
public final class NukeEffects {
    private static final RandomSource RANDOM = RandomSource.create();

    public static void sound(ServerLevel level, Vec3 pos, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, pos.x, pos.y, pos.z, sound, SoundSource.MASTER, volume, pitch);
    }

    /**
     * Carves a clean lunar-style crater.
     *
     * The crater uses a fixed rim plane at the impact surface. For every horizontal position inside
     * the circular radius, the bottom of the crater is calculated from the lower half of a sphere:
     *
     *     floorY = rimY - sqrt(r^2 - dx^2 - dz^2)
     *
     * Everything from that spherical floor up to the open surface is removed. There is no random
     * edge noise, so the final silhouette stays round, continuous and smooth instead of looking
     * bitten, columnar or irregular. The scan is progressive to avoid freezing the server.
     */
    public static int carveLunarCrater(ServerLevel level, Vec3 center, int radius,
                                       int cursor, int scanBudget, int changeBudget) {
        if (!NukeConfig.ALLOW_TERRAIN_DAMAGE.get() || radius <= 0 || scanBudget <= 0 || changeBudget <= 0) {
            return cursor;
        }

        int diameter = radius * 2 + 1;
        int clearAbove = Math.min(16, Math.max(6, radius / 5));
        int verticalCount = radius + clearAbove + 1; // dy = -radius .. +clearAbove
        int columnArea = diameter * diameter;
        int total = columnArea * verticalCount;
        if (cursor < 0 || cursor >= total) return -1;

        int cx = (int) Math.floor(center.x);
        int cz = (int) Math.floor(center.z);
        int rimY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz) - 1;
        double radiusSq = (double) radius * radius;

        int scanned = 0;
        int changed = 0;
        while (cursor < total && scanned < scanBudget && changed < changeBudget) {
            int index = cursor++;
            scanned++;

            int yIndex = index / columnArea;
            int rem = index - yIndex * columnArea;
            int dzIndex = rem / diameter;
            int dxIndex = rem - dzIndex * diameter;

            int dx = dxIndex - radius;
            int dz = dzIndex - radius;
            double horizontalSq = (double) dx * dx + (double) dz * dz;
            if (horizontalSq > radiusSq) continue;

            int dy = yIndex - radius;
            double floorOffset = -Math.sqrt(Math.max(0.0, radiusSq - horizontalSq));

            // Keep the solid ground below the spherical bowl. Remove everything from the bowl
            // surface upward so the opening remains clean even on slightly uneven terrain.
            if (dy + 0.35 < floorOffset) continue;

            int y = rimY + dy;
            if (y <= level.getMinBuildHeight() || y >= level.getMaxBuildHeight()) continue;
            if (removeBlock(level, new BlockPos(cx + dx, y, cz + dz))) changed++;
        }

        return cursor >= total ? -1 : cursor;
    }

    /** Kept for compatibility with older calls. */
    public static int carveSphericalCrater(ServerLevel level, Vec3 center, int radius,
                                           int cursor, int scanBudget, int changeBudget) {
        return carveLunarCrater(level, center, radius, cursor, scanBudget, changeBudget);
    }

    /** Kept for compatibility with older calls; not used by the clean crater path. */
    public static int erodeRandomSphere(ServerLevel level, Vec3 center, int radius, int requestedAttempts) {
        if (!NukeConfig.ALLOW_TERRAIN_DAMAGE.get() || radius <= 0 || requestedAttempts <= 0) return 0;
        int targetChanges = Math.min(requestedAttempts, NukeConfig.MAX_BLOCK_CHANGES_PER_TICK.get());
        int attemptLimit = Math.max(targetChanges + 64, targetChanges * 10);
        int changed = 0;
        int radiusSq = radius * radius;
        int centerX = (int) Math.floor(center.x);
        int centerY = (int) Math.floor(center.y);
        int centerZ = (int) Math.floor(center.z);

        for (int i = 0; i < attemptLimit && changed < targetChanges; i++) {
            int dx = RANDOM.nextInt(radius * 2 + 1) - radius;
            int dy = RANDOM.nextInt(radius * 2 + 1) - radius;
            int dz = RANDOM.nextInt(radius * 2 + 1) - radius;
            if (dx * dx + dy * dy + dz * dz > radiusSq) continue;
            int y = centerY + dy;
            if (y <= level.getMinBuildHeight() || y >= level.getMaxBuildHeight()) continue;
            if (removeBlock(level, new BlockPos(centerX + dx, y, centerZ + dz))) changed++;
        }
        return changed;
    }

    public static int ejectBlocks(ServerLevel level, Vec3 center, int radius, int requested,
                                  double outwardSpeed, double upwardSpeed) {
        if (!NukeConfig.ALLOW_TERRAIN_DAMAGE.get() || radius <= 0 || requested <= 0) return 0;
        int budget = Math.min(Math.min(requested, 64), Math.max(0, NukeConfig.MAX_BLOCK_CHANGES_PER_TICK.get() / 8));
        int spawned = 0;
        int cx = (int) Math.floor(center.x);
        int cz = (int) Math.floor(center.z);

        for (int i = 0; i < budget; i++) {
            double angle = RANDOM.nextDouble() * Math.PI * 2.0;
            double rr = radius * (0.18 + RANDOM.nextDouble() * 0.78);
            int x = cx + (int) Math.round(Math.cos(angle) * rr);
            int z = cz + (int) Math.round(Math.sin(angle) * rr);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
            if (y <= level.getMinBuildHeight() || y >= level.getMaxBuildHeight()) continue;

            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (!canMoveBlock(level, pos, state)) continue;

            FallingBlockEntity falling = FallingBlockEntity.fall(level, pos, state);
            double dx = falling.getX() - center.x;
            double dz = falling.getZ() - center.z;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len < 0.001) {
                dx = Math.cos(angle);
                dz = Math.sin(angle);
                len = 1.0;
            }
            double speed = outwardSpeed * (0.72 + RANDOM.nextDouble() * 0.62);
            falling.setDeltaMovement(dx / len * speed,
                    upwardSpeed * (0.76 + RANDOM.nextDouble() * 0.72),
                    dz / len * speed);
            spawned++;
        }
        return spawned;
    }

    private static boolean canMoveBlock(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.isAir() || state.is(Blocks.BEDROCK)) return false;
        if (!state.getFluidState().isEmpty()) return false;
        if (level.getBlockEntity(pos) != null) return false;
        return state.getDestroySpeed(level, pos) >= 0.0F;
    }

    private static boolean removeBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(Blocks.BEDROCK)) return false;
        if (state.getDestroySpeed(level, pos) < 0.0F) return false;
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        return true;
    }

    private NukeEffects() {}
}
