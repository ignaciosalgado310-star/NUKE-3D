package com.igy.nuke3d.client;

import com.igy.nuke3d.Nuke3D;
import com.igy.nuke3d.NukeTimeline;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Cinematic procedural NUKE renderer. All effects are textured 3D geometry;
 * the Minecraft particle system is deliberately not used.
 */
public final class NukeVisuals {
    private static final ResourceLocation WHITE = tex("white.png");
    private static final ResourceLocation METAL = tex("metal.png");
    private static final ResourceLocation SMOKE = tex("nuke_smoke.png");
    private static final ResourceLocation FIRE = tex("nuke_fire.png");
    private static final ResourceLocation ROCK = tex("planet.png");
    private static final ResourceLocation ROCK_GLOW = tex("planet_glow.png");

    private NukeVisuals() {}

    public static void renderNuke(PoseStack pose, float t, int duration, double damageRadius,
                                  int terrainRadius, float phase, long seed) {
        float impact = NukeTimeline.IMPACT_TICK;
        if (t < impact) {
            renderEntry(pose, t, impact, phase);
            return;
        }

        float after = t - impact;
        float end = Math.max(150.0f, duration - impact + 66.0f);
        float fade = 1.0f - smooth(end - 38.0f, end, after);
        float crater = Math.max(24.0f, terrainRadius);
        float detailPhase = phase + (float) ((seed >>> 11) & 255L) * 0.013f;

        renderFlash(pose, after);
        renderGroundWave(pose, after, crater);
        renderFireball(pose, after, fade, detailPhase);
        renderDebris(pose, after, fade, crater, detailPhase, seed);
        renderShockwaves(pose, after, Math.max(crater * 1.72f, (float) damageRadius * 1.72f));
        renderMushroom(pose, after, fade, crater, detailPhase);
    }

    private static void renderEntry(PoseStack pose, float t, float impact, float phase) {
        if (t < NukeTimeline.MISSILE_ENTRY_TICK) return;

        float q = smooth(NukeTimeline.MISSILE_ENTRY_TICK, impact, t);
        float dive = q * q * (2.18f - 1.18f * q);
        float y = Mth.lerp(dive, 146.0f, 4.35f);
        float heat = smooth(NukeTimeline.MISSILE_ENTRY_TICK, impact - 18.0f, t);
        float pulse = 0.91f + 0.09f * Mth.sin(t * 0.52f);
        float scale = 2.04f + 0.18f * smooth(impact - 34.0f, impact, t);

        // Compressed hot air and smoke around the falling bomb.
        for (int i = 0; i < 9; i++) {
            float a = phase + i * 2.13f + t * (0.008f + i * 0.0007f);
            float spread = (0.26f + i * 0.12f) * (0.55f + q);
            pose.pushPose();
            pose.translate(Mth.cos(a) * spread, y + 12.0f + i * 3.15f, Mth.sin(a) * spread);
            pose.scale(0.78f + i * 0.035f, 1.35f + i * 0.055f, 0.78f + i * 0.035f);
            boolean hot = i < 4;
            VisualMesh.sphere(pose, hot ? FIRE : SMOKE, 1.20f + i * 0.13f, 9, 15,
                    hot ? 255 : 96, hot ? 92 + i * 24 : 88, hot ? 10 : 82,
                    alpha((hot ? 92 : 62) * heat * (1.0f - i * 0.052f)), hot, false);
            pose.popPose();
        }

        pose.pushPose();
        pose.translate(0, y, 0);
        pose.mulPose(Axis.YP.rotationDegrees(phase * 8.0f + Mth.sin(t * 0.055f + phase) * 2.5f));
        pose.mulPose(Axis.XP.rotationDegrees(Mth.sin(t * 0.074f + phase) * (1.15f - q * 0.72f)));
        pose.mulPose(Axis.ZP.rotationDegrees(Mth.cos(t * 0.066f + phase) * (0.92f - q * 0.58f)));
        pose.scale(scale, scale, scale);

        // Heavy segmented body with an inset dark core.
        VisualMesh.cylinder(pose, METAL, 1.22f, 6.82f, 52, 150, 157, 168, 255, false, true);
        VisualMesh.cylinder(pose, METAL, 0.92f, 6.86f, 48, 61, 67, 77, 255, false, true);

        // Integrated warning bands, not floating rings.
        for (int i = 0; i < 3; i++) {
            pose.pushPose();
            pose.translate(0, 1.30f + i * 1.61f, 0);
            VisualMesh.torus(pose, METAL, 1.17f, 0.13f, 48, 9,
                    128 + i * 8, 24, 25, 255, false, true);
            VisualMesh.torus(pose, FIRE, 0.94f, 0.050f, 42, 7,
                    255, 77, 38, 128, true, false);
            pose.popPose();
        }

        // Reinforced two-piece impact nose.
        VisualMesh.cone(pose, METAL, 1.12f, -2.50f, 52, 96, 103, 113, 255, false, true);
        VisualMesh.cone(pose, METAL, 0.72f, -2.82f, 46, 46, 51, 60, 255, false, true);
        VisualMesh.cone(pose, WHITE, 0.25f, -2.93f, 34,
                255, 229, 193, alpha(32 + heat * 90), true, false);

        // Four main fins and four smaller steering fins.
        for (int i = 0; i < 4; i++) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(i * 90.0f));
            pose.translate(1.03f, 4.82f, 0);
            pose.mulPose(Axis.ZP.rotationDegrees(-90.0f));
            pose.scale(1.48f, 1.12f, 0.32f);
            VisualMesh.cone(pose, METAL, 0.95f, 2.15f, 32, 76, 84, 96, 255, false, true);
            pose.popPose();

            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(i * 90.0f + 45.0f));
            pose.translate(0.86f, 5.70f, 0);
            pose.mulPose(Axis.ZP.rotationDegrees(-90.0f));
            pose.scale(0.72f, 0.82f, 0.24f);
            VisualMesh.cone(pose, METAL, 0.67f, 1.31f, 26, 55, 62, 72, 255, false, true);
            pose.popPose();
        }

        // Visible bolt rows give the casing more mechanical detail.
        for (int row = 0; row < 3; row++) {
            for (int i = 0; i < 8; i++) {
                float a = (float) (i * Math.PI * 2.0 / 8.0) + row * 0.26f;
                pose.pushPose();
                pose.translate(Mth.cos(a) * 1.19f, 1.04f + row * 1.88f, Mth.sin(a) * 1.19f);
                VisualMesh.sphere(pose, METAL, 0.095f, 6, 10, 215, 220, 228, 255, false, true);
                pose.popPose();
            }
        }

        // Layered animated engine flame.
        pose.pushPose();
        pose.translate(0, 6.72f, 0);
        VisualMesh.torus(pose, METAL, 1.20f, 0.20f, 48, 10, 53, 59, 69, 255, false, true);
        VisualMesh.torus(pose, METAL, 0.83f, 0.12f, 42, 9, 213, 218, 226, 255, false, true);
        VisualMesh.cylinder(pose, METAL, 0.66f, 0.45f, 36, 42, 46, 54, 255, false, true);
        VisualMesh.cone(pose, FIRE, 0.93f * pulse, 6.25f + 0.82f * Mth.sin(t * 0.46f), 38,
                255, 67, 6, 235, true, false);
        VisualMesh.cone(pose, FIRE, 0.61f * pulse, 5.05f + 0.55f * Mth.sin(t * 0.64f), 34,
                255, 160, 31, 226, true, false);
        VisualMesh.cone(pose, WHITE, 0.29f * pulse, 3.74f, 30,
                255, 247, 229, 214, true, false);
        pose.popPose();
        pose.popPose();
    }

    private static void renderFlash(PoseStack pose, float after) {
        if (after > 8.0f) return;
        float grow = smooth(0.0f, 1.55f, after);
        float life = 1.0f - smooth(1.35f, 8.0f, after);
        float r = 3.1f + 20.5f * grow;
        pose.pushPose();
        pose.translate(0, 1.18f, 0);
        pose.scale(1.0f, 0.79f, 1.0f);
        VisualMesh.sphere(pose, WHITE, r * 0.46f, 18, 34, 255, 255, 252, alpha(255 * life), true, false);
        VisualMesh.sphere(pose, WHITE, r * 0.72f, 18, 36, 255, 232, 185, alpha(188 * life), true, false);
        VisualMesh.sphere(pose, FIRE, r, 20, 38, 255, 115, 18, alpha(126 * life), true, false);
        pose.popPose();
    }

    private static void renderGroundWave(PoseStack pose, float after, float crater) {
        if (after > 34.0f) return;
        float grow = smooth(0.0f, 20.0f, after);
        float life = 1.0f - smooth(10.0f, 34.0f, after);
        float outer = 3.0f + crater * 0.76f * grow;
        pose.pushPose();
        pose.translate(0, 0.34f, 0);
        VisualMesh.annulus(pose, FIRE, outer * 0.42f, outer, 72,
                255, 72, 7, alpha(160 * life), true, false);
        pose.translate(0, 0.06f, 0);
        VisualMesh.torus(pose, WHITE, outer * 0.78f, 0.48f + outer * 0.018f,
                70, 10, 255, 225, 178, alpha(82 * life), true, false);
        pose.popPose();
    }

    private static void renderFireball(PoseStack pose, float after, float fade, float phase) {
        float born = smooth(0.7f, 7.0f, after);
        float grow = smooth(1.5f, 39.0f, after);
        float hotLife = 1.0f - smooth(50.0f, 132.0f, after);
        if (born <= 0.01f || fade <= 0.01f) return;

        float radius = (5.0f + 18.6f * grow) * (0.97f + 0.03f * Mth.sin(after * 0.22f));
        pose.pushPose();
        pose.translate(0, 2.0f + grow * 2.25f, 0);
        pose.scale(1.08f, 0.84f + 0.13f * grow, 1.08f);
        VisualMesh.sphere(pose, FIRE, radius, 24, 44, 255, 56, 5,
                alpha(150 * born * hotLife * fade), true, false);
        VisualMesh.sphere(pose, FIRE, radius * 0.71f, 22, 40, 255, 144, 22,
                alpha(208 * born * hotLife * fade), true, false);
        VisualMesh.sphere(pose, WHITE, radius * 0.27f, 17, 30, 255, 243, 215,
                alpha(158 * born * (1.0f - smooth(11.0f, 38.0f, after))), true, false);
        pose.popPose();

        float shellLife = 1.0f - smooth(60.0f, 148.0f, after);
        for (int i = 0; i < 30; i++) {
            float a = phase + (float) (i * Math.PI * 2.0 / 30.0)
                    + after * (0.0025f + (i % 6) * 0.00042f);
            float rr = radius * (0.26f + (i % 7) * 0.078f);
            float yy = 1.2f + radius * (0.05f + (i % 8) * 0.044f);
            float blob = radius * (0.13f + (i % 6) * 0.022f);
            boolean smoke = i % 6 == 0 || i % 11 == 0;
            pose.pushPose();
            pose.translate(Mth.cos(a) * rr, yy, Mth.sin(a) * rr);
            pose.scale(1.18f + (i % 3) * 0.10f, 0.68f + (i % 5) * 0.085f, 1.18f);
            VisualMesh.sphere(pose, smoke ? SMOKE : FIRE, blob, 11, 18,
                    smoke ? 88 : 255, smoke ? 78 : 74 + (i % 5) * 27, smoke ? 70 : 7,
                    alpha((smoke ? 132 : 174) * born * shellLife * fade), !smoke, false);
            pose.popPose();
        }
    }

    private static void renderDebris(PoseStack pose, float time, float fade, float crater,
                                     float phase, long seed) {
        float life = 1.0f - smooth(58.0f, 150.0f, time);
        if (life <= 0.01f || fade <= 0.01f) return;
        for (int i = 0; i < 44; i++) {
            float pseudo = hash01(seed, i);
            float a = phase + (float) (i * Math.PI * 2.0 / 44.0) + (pseudo - 0.5f) * 0.48f;
            float travel = Math.min(time, 72.0f) * (0.42f + (i % 9) * 0.070f + pseudo * 0.15f);
            travel = Math.min(travel, crater * (0.44f + (i % 8) * 0.084f));
            float arc = travel * (0.52f + (i % 6) * 0.086f)
                    - time * time * (0.0025f + (i % 4) * 0.00030f);
            float size = i % 9 == 0 ? 3.2f + pseudo * 2.1f : 0.74f + (i % 6) * 0.23f;
            pose.pushPose();
            pose.translate(Mth.cos(a) * travel, 0.9f + Math.max(0.0f, arc), Mth.sin(a) * travel);
            pose.mulPose(Axis.YP.rotationDegrees(time * (1.15f + i * 0.075f)));
            pose.mulPose(Axis.XP.rotationDegrees(i * 23.0f + time * 0.62f));
            pose.mulPose(Axis.ZP.rotationDegrees(i * 11.0f - time * 0.35f));
            pose.scale(size * (0.86f + pseudo * 0.38f), size * (0.52f + (i % 5) * 0.10f), size);
            VisualMesh.sphere(pose, ROCK, 1.0f, 6, 9,
                    126 + (i % 5) * 11, 105 + (i % 5) * 9, 87 + (i % 4) * 9,
                    alpha(235 * life * fade), false, true);
            if (i % 5 == 0) {
                VisualMesh.sphere(pose, ROCK_GLOW, 1.035f, 6, 9,
                        255, 70, 9, alpha(58 * life * fade), true, false);
            }
            pose.popPose();
        }
    }

    private static void renderShockwaves(PoseStack pose, float time, float maxRadius) {
        shock(pose, time, maxRadius, 3.0f, 1.72f, 1.28f, 118);
        shock(pose, time, maxRadius * 0.88f, 10.0f, 1.36f, 0.90f, 82);
        shock(pose, time, maxRadius * 0.74f, 18.0f, 1.05f, 0.62f, 58);
    }

    private static void shock(PoseStack pose, float time, float maxRadius, float delay,
                              float speed, float thickness, int opacity) {
        if (time < delay) return;
        float radius = (time - delay) * speed;
        if (radius <= 2.0f || radius >= maxRadius) return;
        float life = 1.0f - radius / maxRadius;
        pose.pushPose();
        pose.translate(0, 0.42f + radius * 0.017f, 0);
        pose.scale(1.0f, 0.14f, 1.0f);
        VisualMesh.torus(pose, SMOKE, radius, thickness + radius * 0.017f, 70, 11,
                195, 182, 164, alpha(opacity * life), false, false);
        pose.popPose();
    }

    private static void renderMushroom(PoseStack pose, float after, float fade,
                                       float crater, float phase) {
        float rise = smooth(10.0f, 145.0f, after);
        if (rise <= 0.01f || fade <= 0.01f) return;
        float heat = 1.0f - smooth(62.0f, 158.0f, after);
        float stemH = 10.0f + 43.0f * rise;
        float stemR = 4.3f + 2.8f * rise;
        float alpha = 232.0f * fade;

        pose.pushPose();
        pose.translate(0, 0.52f, 0);
        VisualMesh.cylinder(pose, SMOKE, stemR, stemH, 40, 82, 76, 72,
                alpha(alpha * 0.88f), false, false);
        VisualMesh.cylinder(pose, SMOKE, stemR * 0.69f, stemH * 0.98f, 36,
                151, 137, 122, alpha(alpha * 0.49f), false, false);
        if (heat > 0.01f) {
            VisualMesh.cylinder(pose, FIRE, stemR * 0.34f, Math.min(stemH, 20.0f), 32,
                    255, 76, 8, alpha(134 * heat * fade), true, false);
        }
        pose.popPose();

        for (int i = 0; i < 24; i++) {
            float h = stemH * (0.045f + i * 0.041f);
            float a = phase + i * 1.67f + after * (0.0058f + (i % 5) * 0.0008f);
            float orbit = stemR * (0.28f + (i % 6) * 0.095f);
            float blob = stemR * (0.50f + (i % 5) * 0.095f);
            pose.pushPose();
            pose.translate(Mth.cos(a) * orbit, h, Mth.sin(a) * orbit);
            pose.scale(1.14f, 0.74f + (i % 5) * 0.075f, 1.14f);
            VisualMesh.sphere(pose, SMOKE, blob, 10, 17,
                    96 + (i % 4) * 15, 88 + (i % 4) * 13, 82 + (i % 4) * 11,
                    alpha(alpha * 0.73f), false, false);
            pose.popPose();
        }

        float capGrow = smooth(20.0f, 142.0f, after);
        float capY = 13.0f + 39.0f * rise;
        float capR = 7.2f + Math.min(crater * 0.48f, 27.0f) * capGrow;

        pose.pushPose();
        pose.translate(0, capY - capR * 0.16f, 0);
        pose.scale(1.22f, 0.22f, 1.22f);
        VisualMesh.sphere(pose, SMOKE, capR * 0.96f, 18, 34,
                92, 85, 79, alpha(alpha * 0.62f), false, false);
        pose.popPose();

        for (int i = 0; i < 46; i++) {
            float a = phase * 0.72f + (float) (i * Math.PI * 2.0 / 46.0)
                    + after * (0.0020f + (i % 7) * 0.00032f);
            float ring = capR * (0.13f + (i % 12) * 0.047f);
            float yy = capY + ((i % 11) - 5.0f) * capR * 0.052f;
            float blob = capR * (0.15f + (i % 7) * 0.021f);
            pose.pushPose();
            pose.translate(Mth.cos(a) * ring, yy, Mth.sin(a) * ring);
            pose.scale(1.31f, 0.62f + (i % 5) * 0.075f, 1.31f);
            VisualMesh.sphere(pose, SMOKE, blob, 11, 19,
                    98 + (i % 5) * 13, 90 + (i % 5) * 11, 83 + (i % 5) * 9,
                    alpha(alpha * (0.72f + (i % 4) * 0.045f)), false, false);
            pose.popPose();
        }

        pose.pushPose();
        pose.translate(0, capY, 0);
        pose.scale(1.18f, 0.45f, 1.18f);
        VisualMesh.sphere(pose, SMOKE, capR, 18, 34,
                116, 107, 99, alpha(alpha * 0.73f), false, false);
        if (heat > 0.01f) {
            VisualMesh.sphere(pose, FIRE, capR * 0.58f, 15, 28,
                    255, 67, 7, alpha(84 * heat * fade), true, false);
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
