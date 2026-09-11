package com.igy.nuke3d.disaster;

import com.igy.nuke3d.config.NukeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Server-side NUKE terrain helpers. No Minecraft particles are used. */
public final class NukeEffects {
    private static final int[][] IMPACT_SAMPLES = {
            {0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };

    public static void sound(ServerLevel level, Vec3 pos, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, pos.x, pos.y, pos.z, sound, SoundSource.MASTER, volume, pitch);
    }

    /**
     * Finds a real solid surface below the requested position. Air, water/lava, leaves and replaceable
     * vegetation do not count as impact terrain. Several points below the missile footprint are checked;
     * the center column is preferred so a nearby tree or wall does not make the bomb float in mid-air.
     */
    public static Vec3 resolveImpactPoint(ServerLevel level, Vec3 requested) {
        int minY = level.getMinBuildHeight();
        int maxY = level.getMaxBuildHeight() - 1;
        int startY = Math.min(maxY, Math.max(minY + 1, (int) Math.ceil(requested.y) + 6));
        int baseX = (int) Math.floor(requested.x);
        int baseZ = (int) Math.floor(requested.z);

        int centerY = findSolidSurfaceY(level, baseX, baseZ, startY, minY);
        if (centerY != Integer.MIN_VALUE) {
            return new Vec3(requested.x, centerY + 1.0, requested.z);
        }

        int bestY = Integer.MIN_VALUE;
        int bestX = baseX;
        int bestZ = baseZ;
        for (int[] sample : IMPACT_SAMPLES) {
            int sx = baseX + sample[0];
            int sz = baseZ + sample[1];
            int y = findSolidSurfaceY(level, sx, sz, startY, minY);
            if (y > bestY) {
                bestY = y;
                bestX = sx;
                bestZ = sz;
            }
        }

        if (bestY == Integer.MIN_VALUE) {
            bestY = minY;
        }
        return new Vec3(bestX + 0.5, bestY + 1.0, bestZ + 0.5);
    }

    private static int findSolidSurfaceY(ServerLevel level, int x, int z, int startY, int minY) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, startY, z);
        for (int y = startY; y >= minY; y--) {
            pos.setY(y);
            BlockState state = level.getBlockState(pos);
            if (isImpactSurface(level, pos, state)) return y;
        }
        return Integer.MIN_VALUE;
    }

    private static boolean isImpactSurface(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.isAir() || state.is(BlockTags.LEAVES)) return false;
        if (!state.getFluidState().isEmpty()) return false;
        if (state.canBeReplaced()) return false;
        return !state.getCollisionShape(level, pos).isEmpty();
    }

    /**
     * Progressive nuclear crater with controlled deterministic irregularity. The overall silhouette
     * remains a readable bowl, but radius, depth and wall shape vary enough that it no longer looks
     * like a mathematically perfect sphere cut out of the map.
     */
    public static int carveNuclearCrater(ServerLevel level, Vec3 center, int radius, long seed,
                                         int cursor, int scanBudget, int changeBudget) {
        if (!NukeConfig.ALLOW_TERRAIN_DAMAGE.get() || radius <= 0 || scanBudget <= 0 || changeBudget <= 0) {
            return -1;
        }

        int rimWidth = Math.max(5, radius / 7);
        int scanRadius = radius + rimWidth;
        int diameter = scanRadius * 2 + 1;
        int clearAbove = Math.min(18, Math.max(7, radius / 5));
        int maxDepth = Math.max(16, (int) (radius * 0.82));
        int verticalCount = maxDepth + clearAbove + 1;
        int columnArea = diameter * diameter;
        int total = columnArea * verticalCount;
        if (cursor < 0 || cursor >= total) return -1;

        int cx = (int) Math.floor(center.x);
        int cz = (int) Math.floor(center.z);
        int rimY = (int) Math.floor(center.y) - 1;

        int scanned = 0;
        int changed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        while (cursor < total && scanned < scanBudget && changed < changeBudget) {
            int index = cursor++;
            scanned++;

            int yIndex = index / columnArea;
            int rem = index - yIndex * columnArea;
            int dzIndex = rem / diameter;
            int dxIndex = rem - dzIndex * diameter;

            int dx = dxIndex - scanRadius;
            int dz = dzIndex - scanRadius;
            double horizontal = Math.sqrt((double) dx * dx + (double) dz * dz);
            double edgeNoise = smoothNoise(cx + dx, cz + dz, seed ^ 0x6A09E667F3BCC909L);
            double localRadius = radius * (0.955 + edgeNoise * 0.095);
            if (horizontal > localRadius) continue;

            double normalized = horizontal / Math.max(1.0, localRadius);
            double bowl = Math.sqrt(Math.max(0.0, 1.0 - normalized * normalized));
            double depthNoise = smoothNoise(cx + dx * 2, cz + dz * 2, seed ^ 0xBB67AE8584CAA73BL);
            double depth = radius * (0.72 + depthNoise * 0.13);
            double wallNoise = (smoothNoise(cx + dx * 3, cz + dz * 3, seed ^ 0x3C6EF372FE94F82BL) - 0.5) * 3.4;
            double floorOffset = -depth * Math.pow(bowl, 0.80) + wallNoise * (0.25 + normalized * 0.75);

            int dy = yIndex - maxDepth;
            int y = rimY + dy;
            if (y <= level.getMinBuildHeight() || y >= level.getMaxBuildHeight()) continue;
            pos.set(cx + dx, y, cz + dz);

            if (dy > floorOffset) {
                if (removeBlock(level, pos)) changed++;
            } else if (dy >= floorOffset - 1.15 && normalized < 0.98) {
                if (scorchExposedBlock(level, pos, seed, dx, dz, normalized)) changed++;
            }
        }

        return cursor >= total ? -1 : cursor;
    }

    public static int carveLunarCrater(ServerLevel level, Vec3 center, int radius,
                                       int cursor, int scanBudget, int changeBudget) {
        return carveNuclearCrater(level, center, radius, 0x4E554B453344L, cursor, scanBudget, changeBudget);
    }

    public static int carveSphericalCrater(ServerLevel level, Vec3 center, int radius,
                                           int cursor, int scanBudget, int changeBudget) {
        return carveLunarCrater(level, center, radius, cursor, scanBudget, changeBudget);
    }

    /**
     * Adds a scorched transition zone and a low static ejecta rim after the crater is carved.
     * Coarse-cell noise causes adjacent blocks to cluster into 2x2/3x3-looking rubble instead of
     * producing thousands of physics entities.
     */
    public static int decorateAftermath(ServerLevel level, Vec3 center, int radius, long seed,
                                        int cursor, int scanBudget, int changeBudget) {
        if (!NukeConfig.ALLOW_TERRAIN_DAMAGE.get() || radius <= 0 || scanBudget <= 0 || changeBudget <= 0) {
            return -1;
        }

        int outer = radius + Math.max(10, radius / 4);
        int diameter = outer * 2 + 1;
        int total = diameter * diameter;
        if (cursor < 0 || cursor >= total) return -1;

        int cx = (int) Math.floor(center.x);
        int cz = (int) Math.floor(center.z);
        int startY = Math.min(level.getMaxBuildHeight() - 1, (int) Math.floor(center.y) + radius + 24);
        int minY = level.getMinBuildHeight();
        int scanned = 0;
        int changed = 0;

        while (cursor < total && scanned < scanBudget && changed < changeBudget) {
            int index = cursor++;
            scanned++;
            int dz = index / diameter - outer;
            int dx = index % diameter - outer;
            double dist = Math.sqrt((double) dx * dx + (double) dz * dz);
            if (dist < radius * 0.76 || dist > outer) continue;

            int x = cx + dx;
            int z = cz + dz;
            int surfaceY = findSolidSurfaceY(level, x, z, startY, minY);
            if (surfaceY == Integer.MIN_VALUE) continue;

            double radial = dist / radius;
            double burnNoise = smoothNoise(x, z, seed ^ 0xA54FF53A5F1D36F1L);
            BlockPos surface = new BlockPos(x, surfaceY, z);

            if (radial <= 1.22 && burnNoise > 0.22) {
                if (scorchSurfaceBlock(level, surface, seed, x, z, radial)) changed++;
            }

            if (radial >= 0.84 && radial <= 1.13) {
                double cluster = smoothNoise(x >> 1, z >> 1, seed ^ 0x510E527FADE682D1L);
                double detail = noise01(x, z, seed ^ 0x9B05688C2B3E6C1FL);
                if (cluster > 0.67 && detail > 0.48) {
                    int height = cluster > 0.84 ? 2 : 1;
                    for (int h = 1; h <= height && changed < changeBudget; h++) {
                        BlockPos rubblePos = surface.above(h);
                        if (!level.getBlockState(rubblePos).isAir()) break;
                        BlockState rubble = rubbleState(seed, x, z, h);
                        level.setBlock(rubblePos, rubble, 3);
                        changed++;
                    }
                }
            }
        }

        return cursor >= total ? -1 : cursor;
    }

    private static boolean scorchExposedBlock(ServerLevel level, BlockPos pos, long seed,
                                               int dx, int dz, double normalized) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(Blocks.BEDROCK) || state.getDestroySpeed(level, pos) < 0.0F) return false;
        BlockState replacement = scorchedState(seed, pos.getX(), pos.getZ(), normalized);
        if (state.is(replacement.getBlock())) return false;
        level.setBlock(pos, replacement, 3);
        return true;
    }

    private static boolean scorchSurfaceBlock(ServerLevel level, BlockPos pos, long seed,
                                               int x, int z, double radial) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(Blocks.BEDROCK) || state.getDestroySpeed(level, pos) < 0.0F) return false;
        BlockState replacement = scorchedState(seed, x, z, radial);
        if (state.is(replacement.getBlock())) return false;
        level.setBlock(pos, replacement, 3);
        return true;
    }

    private static BlockState scorchedState(long seed, int x, int z, double radial) {
        double n = noise01(x, z, seed ^ 0x1F83D9ABFB41BD6BL);
        if (radial < 0.58) {
            if (n > 0.965) return Blocks.MAGMA_BLOCK.defaultBlockState();
            if (n > 0.90) return Blocks.OBSIDIAN.defaultBlockState();
            if (n > 0.68) return Blocks.BLACKSTONE.defaultBlockState();
            if (n > 0.44) return Blocks.BASALT.defaultBlockState();
            return Blocks.DEEPSLATE.defaultBlockState();
        }
        if (n > 0.86) return Blocks.BLACKSTONE.defaultBlockState();
        if (n > 0.67) return Blocks.COBBLESTONE.defaultBlockState();
        if (n > 0.48) return Blocks.ANDESITE.defaultBlockState();
        if (n > 0.30) return Blocks.COARSE_DIRT.defaultBlockState();
        return Blocks.STONE.defaultBlockState();
    }

    private static BlockState rubbleState(long seed, int x, int z, int h) {
        double n = noise01(x + h * 19, z - h * 23, seed ^ 0x5BE0CD19137E2179L);
        if (n > 0.82) return Blocks.BLACKSTONE.defaultBlockState();
        if (n > 0.62) return Blocks.DEEPSLATE.defaultBlockState();
        if (n > 0.42) return Blocks.COBBLESTONE.defaultBlockState();
        if (n > 0.22) return Blocks.ANDESITE.defaultBlockState();
        return Blocks.COARSE_DIRT.defaultBlockState();
    }

    private static boolean removeBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(Blocks.BEDROCK)) return false;
        if (state.getDestroySpeed(level, pos) < 0.0F) return false;
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        return true;
    }

    private static double smoothNoise(int x, int z, long seed) {
        double a = noise01(x >> 2, z >> 2, seed);
        double b = noise01(x >> 3, z >> 3, seed ^ 0xD6E8FEB86659FD93L);
        double c = noise01(x >> 4, z >> 4, seed ^ 0x94D049BB133111EBL);
        return a * 0.52 + b * 0.31 + c * 0.17;
    }

    private static double noise01(int x, int z, long seed) {
        long n = seed;
        n ^= (long) x * 0x9E3779B97F4A7C15L;
        n ^= (long) z * 0xC2B2AE3D27D4EB4FL;
        n ^= n >>> 30;
        n *= 0xBF58476D1CE4E5B9L;
        n ^= n >>> 27;
        n *= 0x94D049BB133111EBL;
        n ^= n >>> 31;
        return (double) (n & 0x1FFFFFFFFFFFFFL) / (double) 0x1FFFFFFFFFFFFFL;
    }

    private NukeEffects() {}
}
