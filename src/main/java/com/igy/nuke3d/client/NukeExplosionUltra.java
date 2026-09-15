package com.igy.nuke3d.client;

import com.igy.nuke3d.NukeTimeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Replacement explosion renderer for NUKE-3D.
 *
 * This intentionally does NOT render the bomb. The existing bomb model remains untouched and is
 * rendered by the legacy renderers only until the synchronized impact tick. From impact onward,
 * this class owns the explosion so the old blast model is completely replaced rather than merely
 * covered by another layer.
 */
public final class NukeExplosionUltra {
    private static final ResourceLocation WHITE = mc("textures/block/white_concrete.png");
    private static final ResourceLocation YELLOW = mc("textures/block/yellow_concrete.png");
    private static final ResourceLocation ORANGE = mc("textures/block/orange_concrete.png");
    private static final ResourceLocation RED = mc("textures/block/red_concrete.png");
    private static final ResourceLocation MAGMA = mc("textures/block/magma.png");
    private static final ResourceLocation BLACK = mc("textures/block/black_concrete.png");
    private static final ResourceLocation DARK_SMOKE = mc("textures/block/gray_concrete.png");
    private static final ResourceLocation LIGHT_SMOKE = mc("textures/block/light_gray_concrete.png");
    private static final ResourceLocation STONE = mc("textures/block/deepslate.png");

    private NukeExplosionUltra() {}

    public static void render(PoseStack pose, float t, int terrainRadius, float phase, long seed) {
        float after = t - NukeTimeline.IMPACT_TICK;
        if (after < 0.0f) return;

        float crater = Math.max(42.0f, terrainRadius);
        float available = Math.max(1.0f, NukeTimeline.SEQUENCE_TICKS - NukeTimeline.IMPACT_TICK);
        float sequenceFade = 1.0f - smooth(available - 20.0f, available, after);
        if (sequenceFade <= 0.001f) return;

        renderInitialFlash(pose, after, crater);
        renderMainFireball(pose, after, crater, phase, sequenceFade);
        renderPlasmaLobes(pose, after, crater, phase, sequenceFade);
        renderGroundFireCrown(pose, after, crater, phase, sequenceFade);
        renderShockFronts(pose, after, crater, sequenceFade);
        renderPyroclasticWall(pose, after, crater, phase, sequenceFade);
        renderMushroomColumn(pose, after, crater, phase, sequenceFade);
        renderDebrisField(pose, after, crater, phase, seed, sequenceFade);
    }

    private static void renderInitialFlash(PoseStack pose, float after, float crater) {
        if (after > 11.0f) return;
        float grow = smooth(0.0f, 2.8f, after);
        float fade = 1.0f - smooth(2.6f, 11.0f, after);
        float r = 5.0f + Math.min(22.0f, crater * 0.43f) * grow;

        pose.pushPose();
        pose.translate(0, 1.2f, 0);
        pose.scale(1.0f, 0.78f, 1.0f);
        VisualMesh.sphere(pose, RED, r * 1.18f, 18, 32,
                255, 32, 2, alpha(78 * fade), true, false);
        VisualMesh.sphere(pose, ORANGE, r, 18, 32,
                255, 92, 4, alpha(135 * fade), true, false);
        VisualMesh.sphere(pose, YELLOW, r * 0.73f, 17, 30,
                255, 205, 35, alpha(205 * fade), true, false);
        VisualMesh.sphere(pose, WHITE, r * 0.42f, 16, 28,
                255, 252, 236, alpha(250 * fade), true, false);
        pose.popPose();
    }

    /** Massive irregular fireball made from several nested shells and offset lobes. */
    private static void renderMainFireball(PoseStack pose, float after, float crater, float phase, float sequenceFade) {
        if (after > 86.0f) return;
        float born = smooth(0.5f, 6.0f, after);
        float grow = smooth(1.5f, 32.0f, after);
        float fade = (1.0f - smooth(58.0f, 86.0f, after)) * sequenceFade;
        if (born <= 0.001f || fade <= 0.001f) return;

        float maxR = Math.min(58.0f, crater * 1.08f);
        float r = (8.5f + maxR * grow) * (0.985f + 0.025f * Mth.sin(after * 0.21f + phase));

        pose.pushPose();
        pose.translate(0, 2.4f + grow * 3.1f, 0);
        pose.scale(1.06f, 0.80f + grow * 0.14f, 1.06f);
        VisualMesh.sphere(pose, MAGMA, r * 1.02f, 22, 42,
                255, 34, 3, alpha(112 * born * fade), true, false);
        VisualMesh.sphere(pose, RED, r * 0.90f, 22, 42,
                255, 45, 5, alpha(145 * born * fade), true, false);
        VisualMesh.sphere(pose, ORANGE, r * 0.72f, 21, 40,
                255, 109, 8, alpha(186 * born * fade), true, false);
        VisualMesh.sphere(pose, YELLOW, r * 0.52f, 20, 38,
                255, 205, 38, alpha(218 * born * fade), true, false);
        VisualMesh.sphere(pose, WHITE, r * 0.245f, 17, 32,
                255, 250, 230, alpha(225 * born * (1.0f - smooth(18.0f, 40.0f, after))), true, false);
        pose.popPose();
    }

    /**
     * Dozens of separate lobes make the blast look turbulent instead of like one perfect sphere.
     * The colors intentionally alternate from white-hot yellow to orange, deep red and magma.
     */
    private static void renderPlasmaLobes(PoseStack pose, float after, float crater, float phase, float sequenceFade) {
        if (after > 92.0f) return;
        float born = smooth(1.0f, 8.0f, after);
        float grow = smooth(2.0f, 34.0f, after);
        float fade = (1.0f - smooth(64.0f, 92.0f, after)) * sequenceFade;
        if (born <= 0.001f || fade <= 0.001f) return;

        float base = 8.0f + Math.min(55.0f, crater * 1.02f) * grow;
        final int count = 52;
        for (int i = 0; i < count; i++) {
            float random = hash01(0x7A91B3C4D5E6F701L, i);
            float a = phase * 0.72f + i * 2.3999632f + after * (0.0035f + (i % 7) * 0.00055f);
            float ring = base * (0.20f + (i % 10) * 0.067f + random * 0.04f);
            float y = 1.2f + base * (0.035f + (i % 9) * 0.048f) + random * 2.4f;
            float blob = base * (0.075f + (i % 6) * 0.014f + random * 0.018f);

            ResourceLocation tex;
            int green;
            int blue;
            switch (i % 7) {
                case 0 -> { tex = WHITE; green = 242; blue = 215; }
                case 1 -> { tex = YELLOW; green = 204; blue = 38; }
                case 2, 3 -> { tex = ORANGE; green = 105 + (i % 3) * 17; blue = 7; }
                case 4, 5 -> { tex = RED; green = 42 + (i % 2) * 13; blue = 5; }
                default -> { tex = MAGMA; green = 32; blue = 3; }
            }

            pose.pushPose();
            pose.translate(Mth.cos(a) * ring, y, Mth.sin(a) * ring);
            pose.mulPose(Axis.YP.rotationDegrees(i * 17.0f + after * (0.55f + (i % 4) * 0.10f)));
            pose.scale(1.28f + random * 0.30f,
                    0.62f + (i % 5) * 0.075f,
                    1.18f + (1.0f - random) * 0.30f);
            VisualMesh.sphere(pose, tex, blob, 9, 15,
                    255, green, blue, alpha((150 + (i % 5) * 15) * born * fade), true, false);
            pose.popPose();
        }
    }

    private static void renderGroundFireCrown(PoseStack pose, float after, float crater, float phase, float sequenceFade) {
        if (after < 1.0f || after > 72.0f) return;
        float grow = smooth(1.0f, 24.0f, after);
        float fade = (1.0f - smooth(46.0f, 72.0f, after)) * sequenceFade;
        float radius = 5.0f + Math.min(54.0f, crater * 1.08f) * grow;

        final int count = 40;
        for (int i = 0; i < count; i++) {
            float a = phase + i * ((float) Math.PI * 2.0f / count) + after * 0.0038f;
            float wobble = 0.84f + (i % 7) * 0.045f;
            float x = Mth.cos(a) * radius * wobble;
            float z = Mth.sin(a) * radius * wobble;
            float h = 5.5f + (i % 8) * 1.45f + 2.2f * Mth.sin(after * 0.19f + i * 0.7f);
            float br = 1.45f + (i % 5) * 0.31f;

            pose.pushPose();
            pose.translate(x, 0.45f, z);
            pose.scale(1.0f, 1.0f + (i % 4) * 0.15f, 1.0f);
            VisualMesh.cone(pose, i % 3 == 0 ? MAGMA : (i % 3 == 1 ? RED : ORANGE),
                    br, Math.max(2.5f, h), 12,
                    255, i % 3 == 2 ? 112 : 48, 5, alpha(142 * fade), true, false);
            pose.popPose();
        }
    }

    /** Three expanding shock fronts plus a thicker dust ring. */
    private static void renderShockFronts(PoseStack pose, float after, float crater, float sequenceFade) {
        if (after < 1.0f || after > 78.0f) return;
        float fade = (1.0f - smooth(50.0f, 78.0f, after)) * sequenceFade;
        float max = Math.max(88.0f, crater * 3.20f);
        float lead = 5.0f + after * 2.62f;

        for (int i = 0; i < 3; i++) {
            float r = lead - i * 10.5f;
            if (r < 3.0f || r > max) continue;
            pose.pushPose();
            pose.translate(0, 0.50f + i * 0.11f, 0);
            ResourceLocation tex = i == 0 ? WHITE : (i == 1 ? YELLOW : ORANGE);
            int g = i == 0 ? 235 : (i == 1 ? 173 : 104);
            int b = i == 0 ? 212 : (i == 1 ? 35 : 8);
            VisualMesh.annulus(pose, tex, Math.max(0.5f, r - (1.35f - i * 0.15f)),
                    r + (1.35f - i * 0.15f), 88,
                    255, g, b, alpha((135 - i * 20) * fade), true, false);
            pose.popPose();
        }

        float dustR = lead - 4.0f;
        if (dustR > 4.0f && dustR < max) {
            pose.pushPose();
            pose.translate(0, 1.0f + dustR * 0.010f, 0);
            pose.scale(1.0f, 0.20f, 1.0f);
            VisualMesh.torus(pose, LIGHT_SMOKE, dustR, 2.0f + dustR * 0.022f,
                    80, 11, 182, 164, 145, alpha(88 * fade), false, false);
            pose.popPose();
        }
    }

    /** Low rolling smoke wall spreading over the ground after the fireball expands. */
    private static void renderPyroclasticWall(PoseStack pose, float after, float crater, float phase, float sequenceFade) {
        if (after < 18.0f) return;
        float grow = smooth(18.0f, 86.0f, after);
        float fade = (1.0f - smooth(98.0f, 123.0f, after)) * sequenceFade;
        if (fade <= 0.001f) return;
        float radius = 11.0f + Math.min(78.0f, crater * 1.55f) * grow;

        final int count = 44;
        for (int i = 0; i < count; i++) {
            float a = phase * 0.53f + i * 2.3999632f + after * 0.0019f;
            float rr = radius * (0.74f + (i % 6) * 0.045f);
            float y = 1.1f + (i % 5) * 0.75f;
            float blob = 3.6f + (i % 7) * 0.55f + grow * 2.7f;
            int shade = 72 + (i % 5) * 14;

            pose.pushPose();
            pose.translate(Mth.cos(a) * rr, y, Mth.sin(a) * rr);
            pose.scale(1.60f + (i % 4) * 0.14f, 0.58f + (i % 3) * 0.10f, 1.42f);
            VisualMesh.sphere(pose, i % 4 == 0 ? LIGHT_SMOKE : DARK_SMOKE, blob, 8, 13,
                    shade + 22, shade + 13, shade + 8, alpha(124 * fade), false, false);
            pose.popPose();
        }
    }

    /** Tall mushroom column and a broad, boiling cap that stays visible through the end of the audio. */
    private static void renderMushroomColumn(PoseStack pose, float after, float crater, float phase, float sequenceFade) {
        if (after < 20.0f) return;
        float rise = smooth(20.0f, 106.0f, after);
        float fade = sequenceFade;
        float stemH = 8.0f + 48.0f * rise;
        float stemR = 3.6f + 3.5f * rise;

        pose.pushPose();
        pose.translate(0, 0.65f, 0);
        VisualMesh.cylinder(pose, DARK_SMOKE, stemR * 1.18f, stemH, 42,
                79, 72, 68, alpha(206 * fade), false, false);
        VisualMesh.cylinder(pose, LIGHT_SMOKE, stemR * 0.78f, stemH * 0.95f, 38,
                138, 123, 109, alpha(126 * fade), false, false);
        if (after < 88.0f) {
            float hot = 1.0f - smooth(56.0f, 88.0f, after);
            VisualMesh.cylinder(pose, MAGMA, stemR * 0.34f, Math.min(stemH, 21.0f), 30,
                    255, 64, 5, alpha(92 * hot * fade), true, false);
        }
        pose.popPose();

        for (int i = 0; i < 28; i++) {
            float a = phase + i * 1.73f + after * (0.004f + (i % 4) * 0.0007f);
            float h = stemH * (0.06f + (i % 14) * 0.066f);
            float orbit = stemR * (0.34f + (i % 6) * 0.12f);
            float blob = stemR * (0.48f + (i % 5) * 0.10f);
            pose.pushPose();
            pose.translate(Mth.cos(a) * orbit, h, Mth.sin(a) * orbit);
            pose.scale(1.18f, 0.76f + (i % 4) * 0.08f, 1.18f);
            VisualMesh.sphere(pose, i % 4 == 0 ? LIGHT_SMOKE : DARK_SMOKE, blob, 8, 13,
                    102 + (i % 4) * 12, 92 + (i % 4) * 10, 85 + (i % 4) * 9,
                    alpha(158 * fade), false, false);
            pose.popPose();
        }

        float capGrow = smooth(34.0f, 112.0f, after);
        float capY = 12.0f + 43.0f * rise;
        float capR = 8.0f + Math.min(34.0f, crater * 0.70f) * capGrow;
        for (int i = 0; i < 48; i++) {
            float a = phase * 0.71f + i * 2.3999632f + after * (0.0022f + (i % 5) * 0.00032f);
            float ring = capR * (0.12f + (i % 12) * 0.061f);
            float y = capY + ((i % 9) - 4.0f) * capR * 0.065f;
            float blob = capR * (0.12f + (i % 7) * 0.018f);
            int shade = 88 + (i % 5) * 13;

            pose.pushPose();
            pose.translate(Mth.cos(a) * ring, y, Mth.sin(a) * ring);
            pose.mulPose(Axis.YP.rotationDegrees(i * 9.0f + after * 0.11f));
            pose.scale(1.34f, 0.58f + (i % 4) * 0.08f, 1.34f);
            VisualMesh.sphere(pose, i % 4 == 0 ? LIGHT_SMOKE : DARK_SMOKE, blob, 9, 14,
                    shade + 20, shade + 12, shade + 7, alpha(174 * fade), false, false);
            pose.popPose();
        }

        if (after < 96.0f) {
            float hot = 1.0f - smooth(68.0f, 96.0f, after);
            pose.pushPose();
            pose.translate(0, capY - capR * 0.05f, 0);
            pose.scale(1.18f, 0.42f, 1.18f);
            VisualMesh.sphere(pose, RED, capR * 0.62f, 13, 24,
                    255, 48, 6, alpha(72 * hot * fade), true, false);
            VisualMesh.sphere(pose, ORANGE, capR * 0.40f, 12, 22,
                    255, 118, 11, alpha(82 * hot * fade), true, false);
            pose.popPose();
        }
    }

    /** Much denser debris field: large terrain chunks, medium fragments and glowing hot pieces. */
    private static void renderDebrisField(PoseStack pose, float after, float crater, float phase,
                                          long seed, float sequenceFade) {
        if (after > 118.0f) return;
        float fade = (1.0f - smooth(88.0f, 118.0f, after)) * sequenceFade;
        float active = smooth(0.0f, 4.0f, after);
        if (fade <= 0.001f || active <= 0.001f) return;

        final int count = 104;
        for (int i = 0; i < count; i++) {
            float random = hash01(seed, i);
            float random2 = hash01(seed ^ 0x5DEECE66DL, i + 137);
            float a = phase + i * 2.3999632f + (random - 0.5f) * 0.70f;
            float speed = 0.56f + (i % 9) * 0.075f + random * 0.22f;
            float travel = Math.min(after * speed, crater * (0.75f + (i % 8) * 0.14f));
            float gravity = after * after * (0.0020f + (i % 5) * 0.00028f);
            float y = 1.1f + travel * (0.56f + (i % 7) * 0.070f) - gravity;

            boolean huge = i % 17 == 0;
            boolean large = i % 8 == 0;
            float size = huge ? 4.0f + random * 3.5f
                    : (large ? 2.1f + random * 2.0f : 0.55f + random * 1.35f);

            pose.pushPose();
            pose.translate(Mth.cos(a) * travel, Math.max(0.45f, y), Mth.sin(a) * travel);
            pose.mulPose(Axis.YP.rotationDegrees(i * 31.0f + after * (1.9f + random * 2.4f)));
            pose.mulPose(Axis.XP.rotationDegrees(i * 17.0f + after * (1.1f + random2 * 1.8f)));
            pose.mulPose(Axis.ZP.rotationDegrees(i * 11.0f - after * (0.8f + random * 1.4f)));
            pose.scale(size * (0.78f + random * 0.55f),
                    size * (0.44f + random2 * 0.45f),
                    size * (0.76f + (1.0f - random) * 0.52f));

            int shade = 72 + (i % 6) * 12;
            VisualMesh.sphere(pose, i % 5 == 0 ? BLACK : STONE, 1.0f, 6, 10,
                    shade + 18, shade + 8, shade, alpha(225 * active * fade), false, true);

            if (i % 3 == 0) {
                VisualMesh.sphere(pose, i % 2 == 0 ? MAGMA : RED, 1.025f, 6, 10,
                        255, i % 2 == 0 ? 54 : 38, 4,
                        alpha((42 + (i % 4) * 12) * active * fade), true, false);
            }
            pose.popPose();
        }
    }

    private static ResourceLocation mc(String path) {
        return new ResourceLocation("minecraft", path);
    }

    private static int alpha(float value) {
        return Mth.clamp((int) value, 0, 255);
    }

    private static float smooth(float start, float end, float value) {
        if (end <= start) return value >= end ? 1.0f : 0.0f;
        float x = Mth.clamp((value - start) / (end - start), 0.0f, 1.0f);
        return x * x * (3.0f - 2.0f * x);
    }

    private static float hash01(long seed, int index) {
        long n = seed ^ (index * 0x9E3779B97F4A7C15L);
        n ^= n >>> 30;
        n *= 0xBF58476D1CE4E5B9L;
        n ^= n >>> 27;
        n *= 0x94D049BB133111EBL;
        n ^= n >>> 31;
        return (float) ((n & 0xFFFFFFL) / (double) 0xFFFFFFL);
    }
}
