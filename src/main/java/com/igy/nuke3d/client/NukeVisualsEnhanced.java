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
    private static final ResourceLocation ORANGE = mc("textures/block/orange_concrete.png");
    private static final ResourceLocation MAGMA = mc("textures/block/magma.png");
    private static final ResourceLocation SMOKE = mc("textures/block/gray_concrete.png");

    private NukeVisualsEnhanced() {}

    public static void render(PoseStack pose, float t, int duration, int terrainRadius, float phase, long seed) {
        float impact = Math.max(16.0f, duration * 0.43f);
        if (t < impact) {
            renderBombDetail(pose, t, impact, phase);
            return;
        }

        float after = t - impact;
        float crater = Math.max(24.0f, terrainRadius);
        renderImpactCore(pose, after, crater, phase);
        renderPressureRings(pose, after, crater);
        renderRisingColumn(pose, after, crater, phase);
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

    private static void renderImpactCore(PoseStack pose, float after, float crater, float phase) {
        if (after > 18.0f) return;
        float grow = smooth(0.0f, 3.0f, after);
        float fade = 1.0f - smooth(5.0f, 18.0f, after);
        float r = 3.5f + Math.min(crater * 0.42f, 22.0f) * grow;

        pose.pushPose();
        pose.translate(0, 1.2f, 0);
        pose.scale(1.0f, 0.82f, 1.0f);
        VisualMesh.sphere(pose, WHITE, r * 0.58f, 18, 34, 255, 250, 232, alpha(245 * fade), true, false);
        VisualMesh.sphere(pose, ORANGE, r * 0.86f, 20, 38, 255, 115, 18, alpha(170 * fade), true, false);
        VisualMesh.sphere(pose, MAGMA, r, 20, 38, 255, 58, 8, alpha(120 * fade), true, false);
        pose.popPose();

        for (int i = 0; i < 12; i++) {
            float a = phase + (float) (i * Math.PI * 2.0 / 12.0);
            float rr = r * (0.62f + (i % 3) * 0.12f);
            pose.pushPose();
            pose.translate(Mth.cos(a) * rr, 0.8f + (i % 4) * 0.42f, Mth.sin(a) * rr);
            VisualMesh.sphere(pose, i % 3 == 0 ? WHITE : ORANGE, r * (0.13f + (i % 4) * 0.02f),
                    10, 16, 255, i % 3 == 0 ? 230 : 96, 18, alpha(150 * fade), true, false);
            pose.popPose();
        }
    }

    private static void renderPressureRings(PoseStack pose, float after, float crater) {
        if (after < 1.0f || after > 34.0f) return;
        float life = 1.0f - smooth(18.0f, 34.0f, after);
        float r1 = 3.0f + after * 1.72f;
        float r2 = Math.max(2.0f, r1 - 5.8f);
        float max = Math.max(36.0f, crater * 1.65f);
        if (r1 > max) return;

        pose.pushPose();
        pose.translate(0, 0.48f, 0);
        VisualMesh.annulus(pose, WHITE, Math.max(0.5f, r1 - 0.95f), r1 + 0.95f, 72,
                255, 235, 210, alpha(125 * life), true, false);
        pose.translate(0, 0.08f, 0);
        VisualMesh.annulus(pose, SMOKE, Math.max(0.5f, r2 - 0.72f), r2 + 0.72f, 72,
                175, 166, 155, alpha(92 * life), false, false);
        pose.popPose();
    }

    private static void renderRisingColumn(PoseStack pose, float after, float crater, float phase) {
        float rise = smooth(8.0f, 92.0f, after);
        if (rise <= 0.0f) return;
        float fade = 1.0f - smooth(96.0f, 150.0f, after);
        if (fade <= 0.01f) return;

        float h = 7.0f + 34.0f * rise;
        float radius = 3.0f + 2.0f * rise;
        pose.pushPose();
        pose.translate(0, 0.6f, 0);
        VisualMesh.cylinder(pose, SMOKE, radius, h, 40, 108, 102, 98, alpha(205 * fade), false, false);
        VisualMesh.cylinder(pose, MAGMA, radius * 0.35f, Math.min(h, 15.0f), 32,
                255, 82, 10, alpha(90 * fade), true, false);
        pose.popPose();

        float capY = 10.0f + 31.0f * rise;
        float capR = 5.5f + Math.min(19.0f, crater * 0.38f) * smooth(20.0f, 100.0f, after);
        for (int i = 0; i < 20; i++) {
            float a = phase + (float) (i * Math.PI * 2.0 / 20.0) + after * 0.0035f;
            float ring = capR * (0.18f + (i % 7) * 0.085f);
            pose.pushPose();
            pose.translate(Mth.cos(a) * ring, capY + ((i % 5) - 2) * 0.75f, Mth.sin(a) * ring);
            pose.scale(1.22f, 0.72f, 1.22f);
            VisualMesh.sphere(pose, SMOKE, capR * (0.18f + (i % 4) * 0.026f), 11, 18,
                    118, 108, 102, alpha(185 * fade), false, false);
            pose.popPose();
        }
    }

    private static void renderHotDebris(PoseStack pose, float after, float crater, float phase, long seed) {
        if (after < 1.0f || after > 62.0f) return;
        float fade = 1.0f - smooth(38.0f, 62.0f, after);
        for (int i = 0; i < 16; i++) {
            float random = hash01(seed, i);
            float a = phase + i * 2.3999632f + random * 0.7f;
            float speed = 0.55f + (i % 5) * 0.12f + random * 0.16f;
            float travel = Math.min(after * speed, crater * (0.55f + (i % 4) * 0.10f));
            float y = 1.0f + travel * (0.45f + (i % 4) * 0.08f) - after * after * 0.0035f;
            pose.pushPose();
            pose.translate(Mth.cos(a) * travel, Math.max(0.6f, y), Mth.sin(a) * travel);
            pose.mulPose(Axis.YP.rotationDegrees(after * (3.0f + i * 0.3f)));
            pose.scale(0.42f + random * 0.65f, 0.28f + random * 0.38f, 0.42f + random * 0.65f);
            VisualMesh.sphere(pose, i % 3 == 0 ? MAGMA : DARK, 1.0f, 7, 10,
                    i % 3 == 0 ? 255 : 70, i % 3 == 0 ? 72 : 64, i % 3 == 0 ? 8 : 60,
                    alpha(220 * fade), i % 3 == 0, true);
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
