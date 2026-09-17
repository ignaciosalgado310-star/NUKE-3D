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

/** Server-side NUKE terrain helpers. No Minecraft particles or debris entities are used. */
public final class NukeEffects {
    private static final int[][] IMPACT_SAMPLES = {
            {0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };

    // Send changes to clients but deliberately avoid expensive neighbor updates during the impact burst.
    // Static rubble is used instead of falling-block entities so the aftermath remains dramatic without
    // creating hundreds of physics entities that would destroy server FPS.
    private static final int FAST_BLOCK_FLAGS = 2;

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
     * Burst-friendly nuclear crater with controlled deterministic irregularity. The entire volume can be
     * processed in one impact cycle, while FAST_BLOCK_FLAGS prevents costly neighbor updates per block.
     */
    public static int carveNuclearCrater(ServerLevel level, Vec3 center, int radius, long seed,
                                         int cursor, int scanBudget, int changeBudget) {
        if (!NukeConfig.ALLOW_TERRAIN_DAMAGE.get() || radius <= 0 || scanBudget <= 0 || changeBudget <= 0) {
            return -1;
        }

        int rimWidth = Math.max(6, radius / 6);
        int scanRadius = radius + rimWidth;
        int diameter = scanRadius * 2 + 1;
        int clearAbove = Math.min(22, Math.max(9, radius / 4));
        int maxDepth = Math.max(18, (int) (radius * 0.86));
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
            double localRadius = radius * (0.95 + edgeNoise * 0.11);
            if (horizontal > localRadius) continue;

            double normalized = horizontal / Math.max(1.0, localRadius);
            double bowl = Math.sqrt(Math.max(0.0, 1.0 - normalized * normalized));
            double depthNoise = smoothNoise(cx + dx * 2, cz + dz * 2, seed ^ 0xBB67AE8584CAA73BL);
            double depth = radius * (0.76 + depthNoise * 0.14);
            double wallNoise = (smoothNoise(cx + dx * 3, cz + dz * 3, seed ^ 0x3C6EF372FE94F82BL) - 0.5) * 4.2;
            double floorOffset = -depth * Math.pow(bowl, 0.78) + wallNoise * (0.22 + normalized * 0.78);

            int dy = yIndex - maxDepth;
            int y = rimY + dy;
            if (y <= level.getMinBuildHeight() || y >= level.getMaxBuildHeight()) continue;
            pos.set(cx + dx, y, cz + dz);

            if (dy > floorOffset) {
                if (removeBlock(level, pos)) changed++;
            } else if (dy >= floorOffset - 1.45 && normalized < 0.99) {
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
     * Builds a broad nuclear wasteland around the crater: scorched terrain, magma and obsidian patches,
     * sparse lava pockets, persistent fires and clustered rubble piles up to four blocks tall.
     */
    public static int decorateAftermath(ServerLevel level, Vec3 center, int radius, long seed,
                                        int cursor, int scanBudget, int changeBudget) {
        if (!NukeConfig.ALLOW_TERRAIN_DAMAGE.get() || radius <= 0 || scanBudget <= 0 || changeBudget <= 0) {
            return -1;
        }

        int outer = radius + Math.max(18, radius / 3);
        int diameter = outer * 2 + 1;
        int total = diameter * diameter;
        if (cursor < 0 || cursor >= total) return -1;

        int cx = (int) Math.floor(center.x);
        int cz = (int) Math.floor(center.z);
        int startY = Math.min(level.getMaxBuildHeight() - 1, (int) Math.floor(center.y) + radius + 28);
        int minY = level.getMinBuildHeight();
        int scanned = 0;
        int changed = 0;

        while (cursor < total && scanned < scanBudget && changed < changeBudget) {
            int index = cursor++;
            scanned++;
            int dz = index / diameter - outer;
            int dx = index % diameter - outer;
            double dist = Math.sqrt((double) dx * dx + (double) dz * dz);
            if (dist < radius * 0.66 || dist > outer) continue;

            int x = cx + dx;
            int z = cz + dz;
            int surfaceY = findSolidSurfaceY(level, x, z, startY, minY);
            if (surfaceY == Integer.MIN_VALUE) continue;

            double radial = dist / radius;
            double burnNoise = smoothNoise(x, z, seed ^ 0xA54FF53A5F1D36F1L);
            BlockPos surface = new BlockPos(x, surfaceY, z);

            if (radial <= 1.36 && burnNoise > 0.12 && changed < changeBudget) {
                if (scorchSurfaceBlock(level, surface, seed, x, z, radial)) changed++;
            }

            if (changed < changeBudget) {
                changed += addNuclearHazard(level, surface, seed, x, z, radial, changeBudget - changed);
            }

            if (radial >= 0.72 && radial <= 1.31 && changed < changeBudget) {
                double cluster = smoothNoise(x >> 1, z >> 1, seed ^ 0x510E527FADE682D1L);
                double detail = noise01(x, z, seed ^ 0x9B05688C2B3E6C1FL);
                BlockState supportingState = level.getBlockState(surface);
                if (cluster > 0.50 && detail > 0.31 && supportingState.getFluidState().isEmpty()) {
                    int height = 1;
                    if (cluster > 0.67) height = 2;
                    if (cluster > 0.79) height = 3;
                    if (cluster > 0.89) height = 4;

                    for (int h = 1; h <= height && changed < changeBudget; h++) {
                        BlockPos rubblePos = surface.above(h);
                        if (!level.getBlockState(rubblePos).isAir()) break;
                        level.setBlock(rubblePos, rubbleState(seed, x, z, h), FAST_BLOCK_FLAGS);
                        changed++;
                    }
                }
            }
        }

        return cursor >= total ? -1 : cursor;
    }

    private static int addNuclearHazard(ServerLevel level, BlockPos surface, long seed,
                                        int x, int z, double radial, int budget) {
        if (budget <= 0 || radial > 1.28) return 0;

        BlockState current = level.getBlockState(surface);
        if (current.isAir() || current.is(Blocks.BEDROCK) || current.getDestroySpeed(level, surface) < 0.0F) {
            return 0;
        }

        int changed = 0;
        double hazard = noise01(x * 3 + 17, z * 3 - 29, seed ^ 0x243F6A8885A308D3L);

        // Sparse lava sources are intentionally rare enough to avoid turning the entire crater into
        // a fluid-update machine, but common enough to read clearly as a nuclear impact zone.
        if (radial <= 1.08 && hazard > 0.989) {
            if (!current.is(Blocks.LAVA)) {
                level.setBlock(surface, Blocks.LAVA.defaultBlockState(), FAST_BLOCK_FLAGS);
                changed++;
            }
            return changed;
        }

        if (radial <= 1.22 && hazard > 0.875 && changed < budget) {
            BlockState base = hazard > 0.935
                    ? Blocks.NETHERRACK.defaultBlockState()
                    : Blocks.MAGMA_BLOCK.defaultBlockState();
            if (!current.is(base.getBlock())) {
                level.setBlock(surface, base, FAST_BLOCK_FLAGS);
                changed++;
            }

            if (hazard > 0.935 && changed < budget) {
                BlockPos above = surface.above();
                BlockState aboveState = level.getBlockState(above);
                if (aboveState.isAir() && aboveState.getFluidState().isEmpty()) {
                    level.setBlock(above, Blocks.FIRE.defaultBlockState(), FAST_BLOCK_FLAGS);
                    changed++;
                }
            }
        }

        return changed;
    }

    private static boolean scorchExposedBlock(ServerLevel level, BlockPos pos, long seed,
                                               int dx, int dz, double normalized) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(Blocks.BEDROCK) || state.getDestroySpeed(level, pos) < 0.0F) return false;
        BlockState replacement = scorchedState(seed, pos.getX(), pos.getZ(), normalized);
        if (state.is(replacement.getBlock())) return false;
        level.setBlock(pos, replacement, FAST_BLOCK_FLAGS);
        return true;
    }

    private static boolean scorchSurfaceBlock(ServerLevel level, BlockPos pos, long seed,
                                               int x, int z, double radial) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(Blocks.BEDROCK) || state.getDestroySpeed(level, pos) < 0.0F) return false;
        BlockState replacement = scorchedState(seed, x, z, radial);
        if (state.is(replacement.getBlock())) return false;
        level.setBlock(pos, replacement, FAST_BLOCK_FLAGS);
        return true;
    }

    private static BlockState scorchedState(long seed, int x, int z, double radial) {
        double n = noise01(x, z, seed ^ 0x1F83D9ABFB41BD6BL);
        if (radial < 0.72) {
            if (n > 0.992) return Blocks.LAVA.defaultBlockState();
            if (n > 0.935) return Blocks.MAGMA_BLOCK.defaultBlockState();
            if (n > 0.84) return Blocks.OBSIDIAN.defaultBlockState();
            if (n > 0.76) return Blocks.CRYING_OBSIDIAN.defaultBlockState();
            if (n > 0.58) return Blocks.BLACKSTONE.defaultBlockState();
            if (n > 0.40) return Blocks.BASALT.defaultBlockState();
            if (n > 0.24) return Blocks.NETHERRACK.defaultBlockState();
            return Blocks.DEEPSLATE.defaultBlockState();
        }
        if (radial < 1.12) {
            if (n > 0.94) return Blocks.MAGMA_BLOCK.defaultBlockState();
            if (n > 0.84) return Blocks.OBSIDIAN.defaultBlockState();
            if (n > 0.66) return Blocks.BLACKSTONE.defaultBlockState();
            if (n > 0.49) return Blocks.BASALT.defaultBlockState();
            if (n > 0.33) return Blocks.NETHERRACK.defaultBlockState();
            return Blocks.DEEPSLATE.defaultBlockState();
        }
        if (n > 0.88) return Blocks.BLACKSTONE.defaultBlockState();
        if (n > 0.70) return Blocks.COBBLESTONE.defaultBlockState();
        if (n > 0.52) return Blocks.ANDESITE.defaultBlockState();
        if (n > 0.34) return Blocks.COARSE_DIRT.defaultBlockState();
        return Blocks.STONE.defaultBlockState();
    }

    private static BlockState rubbleState(long seed, int x, int z, int h) {
        double n = noise01(x + h * 19, z - h * 23, seed ^ 0x5BE0CD19137E2179L);
        if (n > 0.93) return Blocks.MAGMA_BLOCK.defaultBlockState();
        if (n > 0.84) return Blocks.OBSIDIAN.defaultBlockState();
        if (n > 0.76) return Blocks.CRYING_OBSIDIAN.defaultBlockState();
        if (n > 0.61) return Blocks.BLACKSTONE.defaultBlockState();
        if (n > 0.48) return Blocks.BASALT.defaultBlockState();
        if (n > 0.34) return Blocks.DEEPSLATE.defaultBlockState();
        if (n > 0.20) return Blocks.NETHERRACK.defaultBlockState();
        return Blocks.COBBLESTONE.defaultBlockState();
    }

    private static boolean removeBlock(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(Blocks.BEDROCK)) return false;
        if (state.getDestroySpeed(level, pos) < 0.0F) return false;
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), FAST_BLOCK_FLAGS);
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
