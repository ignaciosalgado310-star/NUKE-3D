package com.igy.nuke3d.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Extra high-detail geometry layered over the base NUKE renderer. */
public final class NukeVisualsEnhanced {
    private static final ResourceLocation IRON = mc("textures/block/iron_block.png");
    private static final ResourceLocation DARK = mc("textures/block/black_concrete.png");
    private static final ResourceLocation RED = mc("textures/block/redstone_block.png");
    private static final ResourceLocation WHITE = mc("textures/block/white_concrete.png");
    private static final ResourceLocation YELLOW = mc("textures/block/yellow_concrete.png");
    private static final ResourceLocation ORANGE = mc("textures/block/orange_concrete.png");
    private static final ResourceLocation REDHOT = mc("textures/block/red_concrete.png");
    private static final ResourceLocation MAGMA = mc("textures/block/magma.png");
    private static final ResourceLocation SMOKE = mc("textures/block/gray_concrete.png");

    private NukeVisualsEnhanced() {}

    public static void render(PoseStack pose, float t, int duration, int terrainRadius, float phase, long seed) {
        float impact = Math.max(16.0f, duration * 0.43f);
        if (t < impact) {
            // The bomb model is intentionally left exactly as it was.
            renderBombDetail(pose, t, impact, phase);
            return;
        }

        float after = t - impact;
        float crater = Math.max(24.0f, terrainRadius);

        // Explosion-only upgrade: larger layered fireball, hotter color gradient,
        // stronger pressure fronts, denser mushroom cloud and more visible debris.
        renderImpactCore(pose, after, crater, phase);
        renderPlasmaBloom(pose, after, crater, phase);
        renderPressureRings(pose, after, crater);
        renderGroundFireCrown(pose, after, crater, phase);
        renderRisingColumn(pose, after, crater, phase);
        renderOuterSmokeWall(pose, after, crater, phase);
        renderHotDebris(pose, after, crater, phase, seed);
    }

    private static void renderBombDetail(PoseStack pose, float t, float impact, float phase) {
        float q = smooth(0.0f, impact, t);
        float fallCurve = q * q * (2.0f - q);
        float y = Mth.lerp(fallCurve, 122.0f, 3.88f);
        float yaw = phase * 7.0f + Mth.sin(t * 0.09f + phase) * 2.2f;
        float tiltX = Mth.sin(t * 0.12f + phase * 0.7f) * 1.35f;
        float tiltZ = Mth.cos(t * 0.10f + phase * 1.3f) * 1.05f;
        float pulse = 0.84f + 0.16f * Mth.sin(t * 0.60f);

        pose.pushPose();
        pose.translate(0, y, 0);
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        pose.mulPose(Axis.XP.rotationDegrees(tiltX));
        pose.mulPose(Axis.ZP.rotationDegrees(tiltZ));
        pose.scale(1.72f, 1.72f, 1.72f);

        VisualMesh.cylinder(pose, IRON, 1.135f, 6.16f, 52, 205, 210, 218, 255, false, true);
        VisualMesh.cylinder(pose, DARK, 1.145f, 0.54f, 52, 42, 45, 50, 255, false, true);

        pose.pushPose();
        pose.translate(0, 2.08f, 0);
        VisualMesh.cylinder(pose, DARK, 1.15f, 0.38f, 52, 35, 38, 43, 255, false, true);
        pose.popPose();

        pose.pushPose();
        pose.translate(0, 4.55f, 0);
        VisualMesh.cylinder(pose, RED, 1.16f, 0.34f, 52, 160, 20, 18, 255, false, true);
        pose.popPose();

        pose.pushPose();
        VisualMesh.cone(pose, IRON, 1.02f, -2.34f, 52, 180, 187, 198, 255, false, true);
        VisualMesh.cone(pose, DARK, 0.63f, -2.55f, 46, 47, 49, 54, 255, false, true);
        pose.popPose();

        for (int i = 0; i < 4; i++) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(i * 90.0f));
            pose.translate(1.02f, 5.04f, 0);
            pose.mulPose(Axis.ZP.rotationDegrees(-90.0f));
            pose.scale(1.45f, 0.92f, 0.26f);
            VisualMesh.cone(pose, DARK, 0.88f, 2.05f, 28, 45, 48, 54, 255, false, true);
            pose.popPose();
        }

        for (int i = 0; i < 3; i++) {
            float a = phase + i * 2.0943952f;
            pose.pushPose();
            pose.translate(Mth.cos(a) * 1.18f, 3.25f + i * 0.23f, Mth.sin(a) * 1.18f);
            VisualMesh.sphere(pose, RED, 0.12f + 0.025f * pulse, 8, 12,
                    255, 52, 24, 220, true, false);
            pose.popPose();
        }

        pose.pushPose();
        pose.translate(0, 6.08f, 0);
        VisualMesh.torus(pose, DARK, 1.13f, 0.20f, 48, 10, 42, 44, 49, 255, false, true);
        VisualMesh.cone(pose, ORANGE, 0.90f * pulse, 6.25f, 40, 255, 94, 12, 230, true, false);
        VisualMesh.cone(pose, WHITE, 0.35f * pulse, 4.2f, 34, 255, 238, 210, 210, true, false);
        pose.popPose();

        pose.popPose();
    }

    /**
     * Huge multi-layer core. The white center is kept tighter so shader bloom does not wash out
     * the whole blast; the larger shells carry the yellow/orange/red detail.
     */
    private static void renderImpactCore(PoseStack pose, float after, float crater, float phase) {
        if (after > 30.0f) return;
        float grow = smooth(0.0f, 4.2f, after);
        float fade = 1.0f - smooth(12.0f, 30.0f, after);
        float pulse = 0.96f + 0.04f * Mth.sin(after * 0.78f + phase);
        float r = (7.0f + Math.min(crater * 0.78f, 42.0f) * grow) * pulse;

        pose.pushPose();
        pose.translate(0, 1.35f + grow * 0.75f, 0);
        pose.scale(1.0f, 0.80f, 1.0f);
        VisualMesh.sphere(pose, MAGMA, r * 1.06f, 22, 42,
                255, 42, 4, alpha(112 * fade), true, false);
        VisualMesh.sphere(pose, REDHOT, r * 0.96f, 22, 42,
                255, 48, 10, alpha(145 * fade), true, false);
        VisualMesh.sphere(pose, ORANGE, r * 0.82f, 21, 40,
                255, 105, 10, alpha(188 * fade), true, false);
        VisualMesh.sphere(pose, YELLOW, r * 0.63f, 20, 38,
                255, 206, 52, alpha(218 * fade), true, false);
        VisualMesh.sphere(pose, WHITE, r * 0.34f, 18, 34,
                255, 251, 236, alpha(246 * fade), true, false);
        pose.popPose();

        for (int i = 0; i < 16; i++) {
            float a = phase + (float) (i * Math.PI * 2.0 / 16.0) + after * (0.011f + (i % 3) * 0.002f);
            float rr = r * (0.26f + (i % 5) * 0.105f);
            float y = 0.9f + r * (0.08f + (i % 6) * 0.055f);
            float blob = r * (0.085f + (i % 4) * 0.018f);
            ResourceLocation tex = i % 4 == 0 ? WHITE : (i % 4 == 1 ? YELLOW : ORANGE);
            int red = 255;
            int green = i % 4 == 0 ? 244 : (i % 4 == 1 ? 192 : 96);
            int blue = i % 4 == 0 ? 224 : (i % 4 == 1 ? 35 : 8);
            pose.pushPose();
            pose.translate(Mth.cos(a) * rr, y, Mth.sin(a) * rr);
            pose.scale(1.18f, 0.78f + (i % 3) * 0.09f, 1.18f);
            VisualMesh.sphere(pose, tex, blob, 10, 16,
                    red, green, blue, alpha(176 * fade), true, false);
            pose.popPose();
        }
    }

    private static void renderPlasmaBloom(PoseStack pose, float after, float crater, float phase) {
        if (after < 1.0f || after > 52.0f) return;
        float grow = smooth(1.0f, 15.0f, after);
        float fade = 1.0f - smooth(28.0f, 52.0f, after);
        float base = 8.0f + Math.min(crater * 0.74f, 38.0f) * grow;

        for (int i = 0; i < 34; i++) {
            float a = phase * 0.73f + (float) (i * Math.PI * 2.0 / 34.0)
                    + after * (0.0046f + (i % 5) * 0.0007f);
            float rr = base * (0.28f + (i % 8) * 0.090f);
            float y = 1.1f + base * (0.04f + (i % 7) * 0.058f);
            float blob = base * (0.105f + (i % 6) * 0.018f);

            int variant = i % 5;
            ResourceLocation tex = variant == 0 ? MAGMA : (variant == 1 ? REDHOT : (variant <= 3 ? ORANGE : YELLOW));
            int green = variant == 0 ? 48 : (variant == 1 ? 52 : (variant <= 3 ? 104 + (i % 3) * 22 : 188));
            int blue = variant == 4 ? 28 : 7;

            pose.pushPose();
            pose.translate(Mth.cos(a) * rr, y, Mth.sin(a) * rr);
            pose.mulPose(Axis.YP.rotationDegrees(after * (0.18f + (i % 4) * 0.035f)));
            pose.scale(1.25f + (i % 3) * 0.10f, 0.66f + (i % 4) * 0.09f, 1.25f);
            VisualMesh.sphere(pose, tex, blob, 10, 17,
                    255, green, blue, alpha((150 + (i % 4) * 14) * fade), true, false);
            pose.popPose();
        }
    }

    private static void renderPressureRings(PoseStack pose, float after, float crater) {
        if (after < 1.0f || after > 50.0f) return;
        float life = 1.0f - smooth(30.0f, 50.0f, after);
        float max = Math.max(52.0f, crater * 2.55f);

        float r1 = 4.0f + after * 2.12f;
        float r2 = Math.max(2.0f, r1 - 6.8f);
        float r3 = Math.max(2.0f, r1 - 13.2f);
        if (r3 > max) return;

        pose.pushPose();
        pose.translate(0, 0.52f, 0);
        if (r1 <= max) {
            VisualMesh.annulus(pose, WHITE, Math.max(0.5f, r1 - 1.10f), r1 + 1.10f, 80,
                    255, 244, 224, alpha(138 * life), true, false);
        }
        pose.translate(0, 0.10f, 0);
        if (r2 <= max) {
            VisualMesh.annulus(pose, YELLOW, Math.max(0.5f, r2 - 0.90f), r2 + 0.90f, 76,
                    255, 184, 38, alpha(116 * life), true, false);
        }
        pose.translate(0, 0.10f, 0);
        if (r3 <= max) {
            VisualMesh.annulus(pose, SMOKE, Math.max(0.5f, r3 - 0.78f), r3 + 0.78f, 72,
                    176, 158, 146, alpha(92 * life), false, false);
        }
        pose.popPose();

        if (r1 <= max) {
            pose.pushPose();
            pose.translate(0, 1.10f + r1 * 0.010f, 0);
            pose.scale(1.0f, 0.26f, 1.0f);
            VisualMesh.torus(pose, ORANGE, r1, 1.15f + r1 * 0.016f, 72, 10,
                    255, 112, 20, alpha(88 * life), true, false);
            pose.popPose();
        }
    }

    private static void renderGroundFireCrown(PoseStack pose, float after, float crater, float phase) {
        if (after < 2.0f || after > 44.0f) return;
        float grow = smooth(2.0f, 16.0f, after);
        float fade = 1.0f - smooth(24.0f, 44.0f, after);
        float ring = Math.min(38.0f, crater * 0.82f) * grow + 4.0f;

        for (int i = 0; i < 24; i++) {
            float a = phase + (float) (i * Math.PI * 2.0 / 24.0) + after * 0.005f;
            float jitter = 0.84f + (i % 5) * 0.055f;
            float x = Mth.cos(a) * ring * jitter;
            float z = Mth.sin(a) * ring * jitter;
            float h = 4.8f + (i % 6) * 1.25f + 1.4f * Mth.sin(after * 0.35f + i);
            float br = 1.25f + (i % 4) * 0.28f;
            ResourceLocation tex = i % 3 == 0 ? MAGMA : (i % 3 == 1 ? REDHOT : ORANGE);
            int green = i % 3 == 2 ? 112 : (i % 3 == 1 ? 52 : 62);

            pose.pushPose();
            pose.translate(x, 0.35f, z);
            VisualMesh.sphere(pose, tex, br * 1.35f, 8, 13,
                    255, green, 8, alpha(158 * fade), true, false);
            VisualMesh.cone(pose, tex, br, h, 18,
                    255, green + 18, 8, alpha(174 * fade), true, false);
            pose.popPose();
        }
    }

    private static void renderRisingColumn(PoseStack pose, float after, float crater, float phase) {
        float rise = smooth(7.0f, 108.0f, after);
        if (rise <= 0.0f) return;
        float fade = 1.0f - smooth(142.0f, 198.0f, after);
        if (fade <= 0.01f) return;

        float h = 10.0f + 50.0f * rise;
        float radius = 4.2f + 3.8f * rise;
        float heat = 1.0f - smooth(72.0f, 148.0f, after);

        pose.pushPose();
        pose.translate(0, 0.55f, 0);
        VisualMesh.cylinder(pose, SMOKE, radius, h, 44,
                95, 88, 84, alpha(212 * fade), false, false);
        VisualMesh.cylinder(pose, SMOKE, radius * 0.70f, h * 0.97f, 40,
                142, 128, 116, alpha(116 * fade), false, false);
        if (heat > 0.01f) {
            VisualMesh.cylinder(pose, MAGMA, radius * 0.42f, Math.min(h, 24.0f), 36,
                    255, 72, 8, alpha(112 * heat * fade), true, false);
            VisualMesh.cylinder(pose, ORANGE, radius * 0.22f, Math.min(h, 18.0f), 32,
                    255, 138, 20, alpha(118 * heat * fade), true, false);
        }
        pose.popPose();

        for (int i = 0; i < 24; i++) {
            float a = phase + i * 1.618034f + after * (0.004f + (i % 4) * 0.0008f);
            float y = h * (0.05f + i * 0.039f);
            float orbit = radius * (0.34f + (i % 5) * 0.10f);
            float blob = radius * (0.48f + (i % 4) * 0.11f);
            pose.pushPose();
            pose.translate(Mth.cos(a) * orbit, y, Mth.sin(a) * orbit);
            pose.scale(1.16f, 0.78f + (i % 3) * 0.08f, 1.16f);
            VisualMesh.sphere(pose, SMOKE, blob, 10, 16,
                    104 + (i % 3) * 13, 96 + (i % 3) * 11, 90 + (i % 3) * 9,
                    alpha(174 * fade), false, false);
            pose.popPose();
        }

        float capGrow = smooth(18.0f, 118.0f, after);
        float capY = 14.0f + 43.0f * rise;
        float capR = 9.0f + Math.min(30.0f, crater * 0.58f) * capGrow;

        for (int i = 0; i < 38; i++) {
            float a = phase * 0.67f + (float) (i * Math.PI * 2.0 / 38.0)
                    + after * (0.0024f + (i % 6) * 0.0004f);
            float ring = capR * (0.15f + (i % 11) * 0.058f);
            float oy = capY + ((i % 9) - 4.0f) * capR * 0.052f;
            float blob = capR * (0.14f + (i % 6) * 0.022f);
            int shade = 88 + (i % 5) * 14;
            pose.pushPose();
            pose.translate(Mth.cos(a) * ring, oy, Mth.sin(a) * ring);
            pose.mulPose(Axis.YP.rotationDegrees(after * (0.11f + i * 0.008f)));
            pose.scale(1.32f, 0.62f + (i % 4) * 0.08f, 1.32f);
            VisualMesh.sphere(pose, SMOKE, blob, 11, 18,
                    shade + 16, shade + 8, shade + 2,
                    alpha((176 + (i % 3) * 12) * fade), false, false);
            pose.popPose();
        }

        if (heat > 0.01f) {
            for (int i = 0; i < 16; i++) {
                float a = phase + (float) (i * Math.PI * 2.0 / 16.0) - after * 0.003f;
                float ring = capR * (0.24f + (i % 4) * 0.10f);
                float blob = capR * (0.085f + (i % 4) * 0.016f);
                ResourceLocation tex = i % 3 == 0 ? YELLOW : (i % 3 == 1 ? ORANGE : REDHOT);
                int green = i % 3 == 0 ? 176 : (i % 3 == 1 ? 98 : 48);
                pose.pushPose();
                pose.translate(Mth.cos(a) * ring, capY - capR * (0.10f + (i % 3) * 0.025f), Mth.sin(a) * ring);
                pose.scale(1.34f, 0.58f, 1.34f);
                VisualMesh.sphere(pose, tex, blob, 9, 15,
                        255, green, 8, alpha(132 * heat * fade), true, false);
                pose.popPose();
            }

            pose.pushPose();
            pose.translate(0, capY - capR * 0.08f, 0);
            pose.scale(1.0f, 0.34f, 1.0f);
            VisualMesh.torus(pose, ORANGE, capR * 0.52f, Math.max(0.8f, capR * 0.075f), 64, 10,
                    255, 104, 12, alpha(92 * heat * fade), true, false);
            pose.popPose();
        }
    }

    private static void renderOuterSmokeWall(PoseStack pose, float after, float crater, float phase) {
        if (after < 9.0f || after > 96.0f) return;
        float grow = smooth(9.0f, 48.0f, after);
        float fade = 1.0f - smooth(64.0f, 96.0f, after);
        float ring = 10.0f + Math.min(48.0f, crater * 1.05f) * grow;

        for (int i = 0; i < 26; i++) {
            float a = phase * 0.52f + (float) (i * Math.PI * 2.0 / 26.0) + after * 0.0028f;
            float rr = ring * (0.86f + (i % 4) * 0.045f);
            float y = 1.0f + (i % 6) * 0.72f + grow * (2.0f + (i % 5) * 0.55f);
            float blob = 1.8f + grow * (2.2f + (i % 5) * 0.45f);
            pose.pushPose();
            pose.translate(Mth.cos(a) * rr, y, Mth.sin(a) * rr);
            pose.scale(1.42f, 0.66f + (i % 3) * 0.12f, 1.42f);
            VisualMesh.sphere(pose, SMOKE, blob, 9, 15,
                    116 + (i % 3) * 10, 103 + (i % 3) * 9, 94 + (i % 3) * 8,
                    alpha(122 * fade), false, false);
            pose.popPose();
        }
    }

    private static void renderHotDebris(PoseStack pose, float after, float crater, float phase, long seed) {
        if (after < 1.0f || after > 78.0f) return;
        float fade = 1.0f - smooth(50.0f, 78.0f, after);
        for (int i = 0; i < 28; i++) {
            float random = hash01(seed, i);
            float a = phase + i * 2.3999632f + random * 0.82f;
            float speed = 0.62f + (i % 6) * 0.13f + random * 0.20f;
            float travel = Math.min(after * speed, crater * (0.72f + (i % 5) * 0.13f));
            float y = 1.0f + travel * (0.54f + (i % 5) * 0.085f) - after * after * (0.0030f + (i % 3) * 0.00025f);
            float size = (i % 9 == 0 ? 1.20f : 0.52f) + random * (i % 9 == 0 ? 1.35f : 0.74f);
            ResourceLocation tex = i % 4 == 0 ? MAGMA : (i % 4 == 1 ? REDHOT : DARK);
            int red = i % 4 <= 1 ? 255 : 76;
            int green = i % 4 == 0 ? 74 : (i % 4 == 1 ? 46 : 66);
            int blue = i % 4 <= 1 ? 8 : 62;

            pose.pushPose();
            pose.translate(Mth.cos(a) * travel, Math.max(0.6f, y), Mth.sin(a) * travel);
            pose.mulPose(Axis.YP.rotationDegrees(after * (2.8f + i * 0.22f)));
            pose.mulPose(Axis.XP.rotationDegrees(i * 19.0f + after * 1.1f));
            pose.scale(size * (0.84f + random * 0.28f), size * (0.52f + (i % 4) * 0.12f), size);
            VisualMesh.sphere(pose, tex, 1.0f, 7, 10,
                    red, green, blue, alpha(222 * fade), i % 4 <= 1, true);
            pose.popPose();
        }
    }

    private static ResourceLocation mc(String path) {
        return new ResourceLocation("minecraft", path);
    }

    private static float smooth(float start, float end, float value) {
        if (end <= start) return value >= end ? 1.0f : 0.0f;
        float x = Mth.clamp((value - start) / (end - start), 0.0f, 1.0f);
        return x * x * (3.0f - 2.0f * x);
    }

    private static int alpha(float value) {
        return Mth.clamp((int) value, 0, 255);
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
