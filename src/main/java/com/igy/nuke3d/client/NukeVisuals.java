package com.igy.nuke3d.client;

import com.igy.nuke3d.Nuke3D;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Cinematic procedural NUKE renderer. Every effect is mesh/geometry based: no Minecraft particles
 * are spawned by this renderer.
 */
public final class NukeVisuals {
    private static final ResourceLocation WHITE = tex("white.png");
    private static final ResourceLocation METAL = tex("metal.png");
    private static final ResourceLocation NUKE_SMOKE = tex("nuke_smoke.png");
    private static final ResourceLocation NUKE_FIRE = tex("nuke_fire.png");
    private static final ResourceLocation ROCK = tex("planet.png");
    private static final ResourceLocation ROCK_GLOW = tex("planet_glow.png");

    private NukeVisuals() {}

    public static void renderNuke(PoseStack pose, float t, int duration, double damageRadius,
                                  int terrainRadius, float phase, long seed) {
        float impact = Math.max(16.0f, duration * 0.43f);
        if (t < impact) {
            renderMissile(pose, t, impact, phase);
            return;
        }

        float after = t - impact;
        float visualEnd = Math.max(88.0f, duration - impact + 42.0f);
        float fade = 1.0f - smooth(visualEnd - 28.0f, visualEnd, after);
        float crater = Math.max(24.0f, terrainRadius);
        float detailPhase = phase + (float) ((seed >>> 11) & 0xFFL) * 0.013f;

        renderNuclearFlash(pose, after);
        renderImpactCompression(pose, after, crater, detailPhase);
        renderFireball(pose, after, fade, detailPhase);
        renderTerrainDebris(pose, after, fade, detailPhase, crater, seed);
        renderShockFront(pose, after, Math.max(crater * 1.46f, (float) damageRadius * 1.52f));
        renderMushroomCloud(pose, after, fade, detailPhase, crater);
    }

    /** Larger, heavier missile with restrained movement and extra mechanical geometry. */
    private static void renderMissile(PoseStack pose, float t, float impact, float phase) {
        float q = smooth(0.0f, impact, t);
        float fallCurve = q * q * (2.0f - q);
        float y = Mth.lerp(fallCurve, 122.0f, 3.88f);
        float heat = smooth(0.0f, impact * 0.72f, t);
        float enginePulse = 0.93f + 0.07f * Mth.sin(t * 0.46f);
        float yaw = phase * 7.0f + Mth.sin(t * 0.09f + phase) * 2.2f;
        float tiltX = Mth.sin(t * 0.12f + phase * 0.7f) * 1.35f;
        float tiltZ = Mth.cos(t * 0.10f + phase * 1.3f) * 1.05f;

        pose.pushPose();
        pose.translate(0, y, 0);
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        pose.mulPose(Axis.XP.rotationDegrees(tiltX));
        pose.mulPose(Axis.ZP.rotationDegrees(tiltZ));
        pose.scale(1.72f, 1.72f, 1.72f);

        VisualMesh.cylinder(pose, METAL, 1.10f, 6.12f, 48, 150, 158, 168, 255, false, true);
        VisualMesh.cylinder(pose, METAL, 0.86f, 6.14f, 44, 76, 84, 96, 255, false, true);

        for (int i = 0; i < 4; i++) {
            pose.pushPose();
            pose.translate(0, 0.12f + i * 1.48f, 0);
            VisualMesh.torus(pose, METAL, 1.07f, i == 0 ? 0.15f : 0.10f, 46, 9,
                    i % 2 == 0 ? 210 : 74,
                    i % 2 == 0 ? 216 : 81,
                    i % 2 == 0 ? 224 : 92,
                    255, false, true);
            pose.popPose();
        }

        pose.pushPose();
        VisualMesh.cone(pose, METAL, 0.98f, -2.25f, 48, 103, 111, 124, 255, false, true);
        VisualMesh.cone(pose, METAL, 0.55f, -2.56f, 40, 62, 68, 77, 255, false, true);
        VisualMesh.cone(pose, WHITE, 0.22f, -2.62f, 30,
                255, 242, 218, alpha(32 + heat * 58), true, false);
        pose.popPose();

        for (int i = 0; i < 4; i++) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(i * 90.0f));
            pose.translate(0.96f, 4.58f, 0);
            pose.mulPose(Axis.ZP.rotationDegrees(-90.0f));
            pose.scale(1.25f, 1.0f, 0.30f);
            VisualMesh.cone(pose, METAL, 0.86f, 1.92f, 30, 91, 100, 113, 255, false, true);
            pose.popPose();

            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(i * 90.0f + 45.0f));
            pose.translate(0.78f, 5.32f, 0);
            pose.mulPose(Axis.ZP.rotationDegrees(-90.0f));
            pose.scale(0.64f, 0.72f, 0.22f);
            VisualMesh.cone(pose, METAL, 0.62f, 1.16f, 24, 72, 80, 92, 255, false, true);
            pose.popPose();
        }

        pose.pushPose();
        pose.translate(0, 6.08f, 0);
        VisualMesh.torus(pose, METAL, 1.12f, 0.18f, 46, 10, 69, 76, 88, 255, false, true);
        VisualMesh.torus(pose, METAL, 0.78f, 0.11f, 40, 9, 205, 212, 220, 255, false, true);
        VisualMesh.cone(pose, NUKE_FIRE, 0.82f * enginePulse, 5.75f + 0.70f * Mth.sin(t * 0.48f), 36,
                255, 84, 9, 238, true, false);
        VisualMesh.cone(pose, NUKE_FIRE, 0.53f * enginePulse, 4.65f + 0.48f * Mth.sin(t * 0.67f), 32,
                255, 165, 42, 228, true, false);
        VisualMesh.cone(pose, WHITE, 0.25f * enginePulse, 3.42f, 28,
                255, 244, 224, 205, true, false);
        pose.popPose();

        pose.popPose();
    }

    private static void renderNuclearFlash(PoseStack pose, float after) {
        if (after < 0.0f || after > 5.2f) return;
        float grow = smooth(0.0f, 1.7f, after);
        float life = 1.0f - smooth(1.4f, 5.2f, after);
        float r = 2.4f + 13.0f * grow;

        pose.pushPose();
        pose.translate(0, 1.15f, 0);
        pose.scale(1.0f, 0.82f, 1.0f);
        VisualMesh.sphere(pose, WHITE, r * 0.62f, 16, 30,
                255, 255, 248, alpha(240 * life), true, false);
        VisualMesh.sphere(pose, NUKE_FIRE, r, 18, 34,
                255, 155, 38, alpha(112 * life), true, false);
        pose.popPose();
    }

    private static void renderImpactCompression(PoseStack pose, float after, float crater, float phase) {
        float life = 1.0f - smooth(0.0f, 13.0f, after);
        if (life <= 0.01f) return;
        float expand = smooth(0.0f, 10.0f, after);

        for (int i = 0; i < 10; i++) {
            float a = phase + (float) (i * Math.PI * 2.0 / 10.0);
            float rr = crater * (0.035f + 0.16f * expand) * (0.78f + (i % 4) * 0.09f);
            pose.pushPose();
            pose.translate(Mth.cos(a) * rr, 0.42f + (i % 3) * 0.18f, Mth.sin(a) * rr);
            pose.scale(1.55f, 0.42f, 1.15f);
            VisualMesh.sphere(pose, NUKE_FIRE, crater * (0.050f + (i % 3) * 0.010f), 9, 14,
                    255, 86 + (i % 3) * 28, 8, alpha(168 * life), true, false);
            pose.popPose();
        }
    }

    private static void renderFireball(PoseStack pose, float after, float fade, float phase) {
        float born = smooth(1.0f, 9.0f, after);
        float grow = smooth(2.0f, 34.0f, after);
        float hotLife = 1.0f - smooth(43.0f, 104.0f, after);
        if (born <= 0.01f || fade <= 0.01f) return;

        float radius = 4.2f + 14.8f * grow;
        pose.pushPose();
        pose.translate(0, 2.1f + grow * 1.6f, 0);
        pose.scale(1.05f, 0.88f + 0.10f * grow, 1.05f);
        VisualMesh.sphere(pose, NUKE_FIRE, radius * 0.94f, 22, 40,
                255, 70, 7, alpha(162 * born * hotLife * fade), true, false);
        VisualMesh.sphere(pose, NUKE_FIRE, radius * 0.68f, 20, 36,
                255, 151, 28, alpha(205 * born * hotLife * fade), true, false);
        VisualMesh.sphere(pose, WHITE, radius * 0.25f, 15, 28,
                255, 240, 206, alpha(145 * born * (1.0f - smooth(10.0f, 32.0f, after))), true, false);
        pose.popPose();

        float shellLife = 1.0f - smooth(52.0f, 122.0f, after);
        for (int i = 0; i < 24; i++) {
            float a = phase + (float) (i * Math.PI * 2.0 / 24.0) + after * (0.0028f + (i % 5) * 0.0005f);
            float rr = radius * (0.30f + (i % 6) * 0.085f);
            float y = 1.2f + radius * (0.06f + (i % 7) * 0.050f);
            float blob = radius * (0.16f + (i % 5) * 0.024f);
            pose.pushPose();
            pose.translate(Mth.cos(a) * rr, y, Mth.sin(a) * rr);
            pose.scale(1.15f + (i % 3) * 0.12f, 0.72f + (i % 4) * 0.10f, 1.15f);
            VisualMesh.sphere(pose, i % 5 == 0 ? NUKE_SMOKE : NUKE_FIRE, blob, 11, 18,
                    i % 5 == 0 ? 82 : 255,
                    i % 5 == 0 ? 72 : 88 + (i % 4) * 27,
                    i % 5 == 0 ? 66 : 9,
                    alpha((i % 5 == 0 ? 126 : 170) * born * shellLife * fade),
                    i % 5 != 0, false);
            pose.popPose();
        }
    }

    private static void renderTerrainDebris(PoseStack pose, float time, float fade, float phase,
                                            float crater, long seed) {
        float life = 1.0f - smooth(48.0f, 128.0f, time);
        if (life <= 0.01f || fade <= 0.01f) return;

        final int count = 36;
        for (int i = 0; i < count; i++) {
            float pseudo = hash01(seed, i);
            float a = phase + (float) (i * Math.PI * 2.0 / count) + (pseudo - 0.5f) * 0.42f;
            float speed = 0.38f + (i % 8) * 0.075f + pseudo * 0.12f;
            float travel = Math.min(time, 64.0f) * speed;
            float maxTravel = crater * (0.42f + (i % 7) * 0.085f);
            travel = Math.min(travel, maxTravel);
            float arc = travel * (0.48f + (i % 5) * 0.095f) - time * time * (0.0026f + (i % 3) * 0.00035f);
            float big = (i % 8 == 0) ? 2.7f + pseudo * 1.8f : 0.72f + (i % 5) * 0.24f;

            pose.pushPose();
            pose.translate(Mth.cos(a) * travel, 0.9f + Math.max(0.0f, arc), Mth.sin(a) * travel);
            pose.mulPose(Axis.YP.rotationDegrees(time * (1.2f + i * 0.08f)));
            pose.mulPose(Axis.XP.rotationDegrees(i * 23.0f + time * 0.64f));
            pose.mulPose(Axis.ZP.rotationDegrees(i * 11.0f - time * 0.37f));
            pose.scale(big * (0.88f + pseudo * 0.34f), big * (0.55f + (i % 4) * 0.10f), big);
            VisualMesh.sphere(pose, ROCK, 1.0f, 6, 9,
                    128 + (i % 4) * 12, 108 + (i % 4) * 10, 91 + (i % 3) * 10,
                    alpha(232 * life * fade), false, true);
            if (i % 6 == 0) {
                VisualMesh.sphere(pose, ROCK_GLOW, 1.025f, 6, 9,
                        255, 77, 12, alpha(46 * life * fade), true, false);
            }
            pose.popPose();
        }
    }

    private static void renderShockFront(PoseStack pose, float time, float maxRadius) {
        if (time < 4.0f) return;
        float radius = (time - 4.0f) * 1.55f;
        if (radius <= 2.0f || radius >= maxRadius) return;
        float life = 1.0f - radius / maxRadius;

        pose.pushPose();
        pose.translate(0, 0.45f + radius * 0.015f, 0);
        pose.scale(1.0f, 0.16f, 1.0f);
        VisualMesh.torus(pose, NUKE_SMOKE, radius, 1.10f + radius * 0.018f,
                66, 11, 185, 174, 160, alpha(104 * life), false, false);
        pose.popPose();
    }

    private static void renderMushroomCloud(PoseStack pose, float after, float fade, float phase, float crater) {
        float rise = smooth(12.0f, 112.0f, after);
        if (rise <= 0.01f || fade <= 0.01f) return;

        float smokeA = 228.0f * fade;
        float heatLife = 1.0f - smooth(52.0f, 132.0f, after);
        float stemH = 9.0f + 35.0f * rise;
        float stemR = 3.8f + 2.1f * rise;

        pose.pushPose();
        pose.translate(0, 0.55f, 0);
        VisualMesh.cylinder(pose, NUKE_SMOKE, stemR, stemH, 38,
                92, 84, 78, alpha(smokeA * 0.84f), false, false);
        VisualMesh.cylinder(pose, NUKE_SMOKE, stemR * 0.68f, stemH * 0.96f, 34,
                158, 143, 126, alpha(smokeA * 0.46f), false, false);
        if (heatLife > 0.01f) {
            VisualMesh.cylinder(pose, NUKE_FIRE, stemR * 0.34f, Math.min(stemH, 16.0f), 30,
                    255, 87, 11, alpha(126 * heatLife * fade), true, false);
        }
        pose.popPose();

        for (int i = 0; i < 18; i++) {
            float h = stemH * (0.06f + i * 0.052f);
            float a = phase + i * 1.71f + after * (0.006f + (i % 4) * 0.001f);
            float orbit = stemR * (0.32f + (i % 5) * 0.10f);
            float blob = stemR * (0.56f + (i % 4) * 0.10f);
            pose.pushPose();
            pose.translate(Mth.cos(a) * orbit, h, Mth.sin(a) * orbit);
            pose.scale(1.12f, 0.78f + (i % 4) * 0.08f, 1.12f);
            VisualMesh.sphere(pose, NUKE_SMOKE, blob, 10, 17,
                    105 + (i % 3) * 15, 96 + (i % 3) * 13, 89 + (i % 3) * 11,
                    alpha(smokeA * 0.70f), false, false);
            pose.popPose();
        }

        float capGrow = smooth(22.0f, 106.0f, after);
        float capY = 12.0f + 31.0f * rise;
        float capR = 6.4f + Math.min(crater * 0.42f, 21.0f) * capGrow;

        for (int i = 0; i < 34; i++) {
            float a = phase * 0.7f + (float) (i * Math.PI * 2.0 / 34.0)
                    + after * (0.0022f + (i % 6) * 0.00035f);
            float ring = capR * (0.16f + (i % 10) * 0.050f);
            float oy = capY + ((i % 9) - 4.0f) * capR * 0.060f;
            float blob = capR * (0.18f + (i % 6) * 0.024f);
            pose.pushPose();
            pose.translate(Mth.cos(a) * ring, oy, Mth.sin(a) * ring);
            pose.mulPose(Axis.YP.rotationDegrees(after * (0.12f + i * 0.009f)));
            pose.scale(1.28f, 0.66f + (i % 4) * 0.08f, 1.28f);
            VisualMesh.sphere(pose, NUKE_SMOKE, blob, 11, 19,
                    104 + (i % 4) * 14, 96 + (i % 4) * 12, 89 + (i % 4) * 10,
                    alpha(smokeA * (0.74f + (i % 3) * 0.05f)), false, false);
            pose.popPose();
        }

        pose.pushPose();
        pose.translate(0, capY, 0);
        pose.scale(1.16f, 0.48f, 1.16f);
        VisualMesh.sphere(pose, NUKE_SMOKE, capR * 0.92f, 16, 30,
                123, 113, 104, alpha(smokeA * 0.72f), false, false);
        if (heatLife > 0.01f) {
            VisualMesh.sphere(pose, NUKE_FIRE, capR * 0.54f, 14, 26,
                    255, 78, 9, alpha(76 * heatLife * fade), true, false);
        }
        pose.popPose();
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

    private static ResourceLocation tex(String name) {
        return new ResourceLocation(Nuke3D.MOD_ID, "textures/fx/" + name);
    }

    private static int alpha(float value) {
        return Mth.clamp((int) value, 0, 255);
    }

    private static float smooth(float start, float end, float value) {
        if (end <= start) return value >= end ? 1.0f : 0.0f;
        float x = Mth.clamp((value - start) / (end - start), 0.0f, 1.0f);
        return x * x * (3.0f - 2.0f * x);
    }
}
