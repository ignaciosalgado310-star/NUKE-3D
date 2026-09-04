package com.igy.nuke3d.client;

import com.igy.nuke3d.Nuke3D;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** High-detail procedural NUKE renderer. No Minecraft particle effects are used. */
public final class NukeVisuals {
    private static final ResourceLocation WHITE = tex("white.png");
    private static final ResourceLocation METAL = tex("metal.png");
    private static final ResourceLocation NUKE_SMOKE = tex("nuke_smoke.png");
    private static final ResourceLocation NUKE_FIRE = tex("nuke_fire.png");
    private static final ResourceLocation PLANET = tex("planet.png");
    private static final ResourceLocation PLANET_GLOW = tex("planet_glow.png");

    private NukeVisuals() {}

    public static void renderNuke(PoseStack pose, float t, int duration, double damageRadius,
                                  int terrainRadius, float phase, long seed) {
        float impact = Math.max(16.0f, Math.min(duration * 0.43f, 78.0f));
        if (t < impact) {
            renderMissile(pose, t, impact, phase);
            return;
        }

        float after = t - impact;
        float born = smooth(0.0f, 8.0f, after);
        float rise = smooth(0.0f, 105.0f, after);
        float capGrow = smooth(0.0f, 82.0f, after);
        float fade = 1.0f - smooth(Math.max(100.0f, duration - impact - 28.0f), duration - impact + 20.0f, after);
        int smokeA = alpha(252 * born * fade);
        int hotA = alpha(255 * born * (1.0f - smooth(46.0f, 138.0f, after)));
        float detailPhase = phase + (float) ((seed >>> 11) & 0xFFL) * 0.011f;

        renderInitialBlast(pose, after, born, hotA);

        float stemH = 9.0f + 34.0f * rise;
        renderStem(pose, after, stemH, rise, smokeA, hotA, detailPhase);

        float capY = 11.0f + 29.0f * rise;
        float capR = 6.2f + 20.8f * capGrow;
        renderMushroomCap(pose, after, capY, capR, smokeA, hotA, detailPhase);

        float crater = Math.max(22.0f, terrainRadius);
        renderGroundBlast(pose, after, crater, hotA);

        renderImpactChunks(pose, after, detailPhase, 34, crater * 0.86f, PLANET, PLANET_GLOW,
                148, 126, 105, 255, 91, 20, 1.14f);
        renderShockRings(pose, after, Math.max(crater * 1.30f, (float) damageRadius * 1.34f), NUKE_FIRE,
                255, 188, 112, 242, 6);
    }

    private static void renderMissile(PoseStack pose, float t, float impact, float phase) {
        float q = smooth(0.0f, impact, t);
        float y = Mth.lerp(q, 88.0f, 2.30f);
        float heat = smooth(0.0f, impact * 0.72f, t);
        float pulse = 0.93f + 0.07f * Mth.sin(t * 0.58f);
        float roll = t * 3.4f + phase * 11.0f;

        pose.pushPose();
        pose.translate(0, y, 0);
        pose.mulPose(Axis.YP.rotationDegrees(roll));

        // Main bomb casing: layered metal makes the body read as a real 3D object instead of one tube.
        pose.pushPose();
        VisualMesh.cylinder(pose, METAL, 1.08f, 6.05f, 44, 150, 158, 168, 255, false, true);
        VisualMesh.cylinder(pose, METAL, 0.82f, 6.08f, 40, 86, 94, 105, 255, false, true);
        pose.translate(0, 0.03f, 0);
        VisualMesh.torus(pose, METAL, 1.04f, 0.12f, 44, 9, 215, 220, 226, 255, false, true);
        pose.translate(0, 2.02f, 0);
        VisualMesh.torus(pose, METAL, 1.03f, 0.105f, 44, 8, 68, 74, 84, 255, false, true);
        pose.translate(0, 2.02f, 0);
        VisualMesh.torus(pose, METAL, 1.03f, 0.105f, 44, 8, 68, 74, 84, 255, false, true);
        pose.popPose();

        // Rounded lower nose. At impact its tip nearly touches the ground.
        pose.pushPose();
        VisualMesh.torus(pose, METAL, 0.93f, 0.13f, 42, 9, 205, 211, 218, 255, false, true);
        VisualMesh.cone(pose, METAL, 0.94f, -2.22f, 44, 108, 116, 128, 255, false, true);
        VisualMesh.cone(pose, WHITE, 0.25f, -2.30f, 30, 235, 242, 249, alpha(48 + heat * 82), true, false);
        pose.popPose();

        // Four large tail fins, built from rotated 3D cones.
        for (int i = 0; i < 4; i++) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(i * 90.0f));
            pose.translate(0.82f, 4.72f, 0);
            pose.mulPose(Axis.ZP.rotationDegrees(-90.0f));
            pose.scale(1.0f, 1.0f, 0.28f);
            VisualMesh.cone(pose, METAL, 0.78f, 1.65f, 28, 104, 112, 123, 255, false, true);
            pose.popPose();
        }

        // Engine/nozzle and layered flame.
        pose.pushPose();
        pose.translate(0, 6.03f, 0);
        VisualMesh.torus(pose, METAL, 1.10f, 0.18f, 44, 10, 74, 81, 92, 255, false, true);
        VisualMesh.torus(pose, METAL, 0.76f, 0.11f, 40, 9, 206, 212, 220, 255, false, true);
        VisualMesh.cone(pose, NUKE_FIRE, 0.78f * pulse, 5.15f + 1.25f * Mth.sin(t * 0.63f), 34,
                255, 104, 16, 238, true, false);
        VisualMesh.cone(pose, NUKE_FIRE, 0.49f * pulse, 4.25f + 0.65f * Mth.sin(t * 0.79f), 30,
                255, 177, 54, 226, true, false);
        VisualMesh.cone(pose, WHITE, 0.25f * pulse, 3.48f, 28,
                255, 246, 221, 212, true, false);
        pose.popPose();

        // Atmospheric heating rings intensify during the final approach.
        for (int i = 0; i < 4; i++) {
            pose.pushPose();
            pose.translate(0, 0.95f + i * 1.30f, 0);
            pose.mulPose(Axis.YP.rotationDegrees((-1.0f + i * 0.31f) * t * 8.4f));
            pose.mulPose(Axis.XP.rotationDegrees(50.0f + i * 11.0f));
            VisualMesh.torus(pose, i >= 2 ? WHITE : NUKE_FIRE,
                    1.42f + heat * 0.45f + i * 0.20f, 0.075f + i * 0.024f, 44, 8,
                    255, 118 + i * 34, 22 + i * 42, alpha((84 + i * 29) * heat), true, false);
            pose.popPose();
        }

        pose.popPose();
    }

    private static void renderInitialBlast(PoseStack pose, float after, float born, int hotA) {
        float flashGrow = smooth(0.0f, 24.0f, after);
        float flash = 4.0f + 11.5f * flashGrow;
        float compression = 1.0f - smooth(18.0f, 62.0f, after);

        pose.pushPose();
        pose.translate(0, 1.45f, 0);
        pose.scale(1.0f, 0.78f + flashGrow * 0.22f, 1.0f);
        VisualMesh.sphere(pose, NUKE_FIRE, flash * 1.38f, 24, 46, 255, 64, 8, alpha(hotA * 0.72f), true, false);
        VisualMesh.sphere(pose, NUKE_FIRE, flash, 26, 50, 255, 144, 36, hotA, true, false);
        VisualMesh.sphere(pose, WHITE, flash * 0.50f, 20, 38, 255, 251, 231, alpha(hotA * 0.94f), true, false);
        pose.popPose();

        if (compression > 0.01f) {
            pose.pushPose();
            pose.translate(0, 1.0f, 0);
            pose.scale(1.0f, 0.24f, 1.0f);
            VisualMesh.torus(pose, WHITE, 4.5f + after * 0.58f, 1.05f + after * 0.025f, 56, 10,
                    255, 238, 200, alpha(210 * born * compression), true, false);
            VisualMesh.sphere(pose, NUKE_FIRE, 6.0f + after * 0.22f, 18, 36,
                    255, 94, 14, alpha(120 * born * compression), true, false);
            pose.popPose();
        }
    }

    private static void renderStem(PoseStack pose, float after, float stemH, float rise,
                                   int smokeA, int hotA, float phase) {
        pose.pushPose();
        pose.translate(0, 0.42f, 0);
        VisualMesh.cylinder(pose, NUKE_SMOKE, 4.25f + 2.30f * rise, stemH, 44,
                132, 127, 124, smokeA, false, false);
        VisualMesh.cylinder(pose, NUKE_SMOKE, 2.78f + 1.72f * rise, stemH * 0.96f, 40,
                204, 188, 166, alpha(smokeA * 0.64f), false, false);
        VisualMesh.cylinder(pose, NUKE_FIRE, 1.72f + 1.05f * rise, Math.min(stemH, 17.5f), 36,
                255, 104, 18, hotA, true, false);
        pose.popPose();

        // Layered rolling smoke around the stem removes the straight "pipe" silhouette.
        for (int i = 0; i < 12; i++) {
            float h = stemH * (0.10f + i * 0.072f);
            float a = phase + i * 1.83f + after * (0.010f + (i % 3) * 0.002f);
            float ring = 1.8f + rise * 2.7f + (i % 4) * 0.42f;
            float blob = 2.3f + rise * 1.55f + (i % 3) * 0.35f;
            pose.pushPose();
            pose.translate(Mth.cos(a) * ring, h, Mth.sin(a) * ring);
            pose.scale(1.15f, 0.82f + (i % 4) * 0.08f, 1.15f);
            VisualMesh.sphere(pose, NUKE_SMOKE, blob, 13, 24,
                    142 + (i % 3) * 13, 135 + (i % 3) * 11, 126 + (i % 3) * 9,
                    alpha(smokeA * 0.82f), false, false);
            pose.popPose();
        }
    }

    private static void renderMushroomCap(PoseStack pose, float after, float capY, float capR,
                                          int smokeA, int hotA, float phase) {
        // Dense rolling outer cap.
        for (int i = 0; i < 34; i++) {
            float a = (float) (i * Math.PI * 2.0 / 34.0 + phase * 0.43 + after * (0.0036 + (i % 5) * 0.0007));
            float ring = capR * (0.18f + 0.043f * (i % 10));
            float ox = Mth.cos(a) * ring;
            float oz = Mth.sin(a) * ring;
            float oy = capY + ((i % 7) - 3.0f) * capR * 0.070f;
            float blob = capR * (0.225f + (i % 6) * 0.028f);
            pose.pushPose();
            pose.translate(ox, oy, oz);
            pose.mulPose(Axis.YP.rotationDegrees(after * (0.22f + i * 0.016f)));
            pose.scale(1.42f, 0.58f + (i % 4) * 0.082f, 1.42f);
            VisualMesh.sphere(pose, NUKE_SMOKE, blob, 15, 28,
                    158 + (i % 4) * 10, 148 + (i % 4) * 9, 138 + (i % 4) * 8,
                    alpha(smokeA * (0.82f + (i % 4) * 0.045f)), false, false);
            pose.popPose();
        }

        // Thick central mushroom mass and glowing underside.
        pose.pushPose();
        pose.translate(0, capY - capR * 0.08f, 0);
        pose.scale(1.18f, 0.38f, 1.18f);
        VisualMesh.sphere(pose, NUKE_SMOKE, capR * 1.06f, 20, 40,
                176, 163, 149, alpha(smokeA * 0.92f), false, false);
        VisualMesh.sphere(pose, NUKE_FIRE, capR * 0.88f, 20, 40,
                255, 82, 12, alpha(hotA * 0.84f), true, false);
        VisualMesh.sphere(pose, WHITE, capR * 0.40f, 18, 34,
                255, 231, 184, alpha(hotA * 0.50f), true, false);
        pose.popPose();

        pose.pushPose();
        pose.translate(0, capY - capR * 0.06f, 0);
        VisualMesh.torus(pose, NUKE_SMOKE, capR * 0.76f, Math.max(0.72f, capR * 0.10f), 60, 12,
                145, 137, 130, alpha(smokeA * 0.62f), false, false);
        VisualMesh.torus(pose, NUKE_FIRE, capR * 0.63f, Math.max(0.44f, capR * 0.045f), 58, 10,
                255, 106, 19, alpha(hotA * 0.58f), true, false);
        pose.popPose();

        // Hot lobes rolling around the lower mushroom edge.
        float hotLife = 1.0f - smooth(42.0f, 112.0f, after);
        if (hotLife > 0.01f) {
            for (int i = 0; i < 10; i++) {
                float a = phase + (float) (i * Math.PI * 2.0 / 10.0) + after * 0.009f;
                float rr = capR * (0.42f + (i % 3) * 0.08f);
                pose.pushPose();
                pose.translate(Mth.cos(a) * rr, capY - capR * 0.20f, Mth.sin(a) * rr);
                pose.scale(1.25f, 0.55f, 1.25f);
                VisualMesh.sphere(pose, NUKE_FIRE, capR * (0.16f + (i % 3) * 0.022f), 12, 22,
                        255, 118, 22, alpha(145 * hotLife), true, false);
                pose.popPose();
            }
        }
    }

    private static void renderGroundBlast(PoseStack pose, float after, float crater, int hotA) {
        float dustLife = 1.0f - smooth(60.0f, 160.0f, after);
        float heatLife = 1.0f - smooth(34.0f, 105.0f, after);

        pose.pushPose();
        pose.translate(0, 0.16f, 0);
        VisualMesh.annulus(pose, PLANET, crater * 0.44f, crater * 1.06f, 76,
                80, 67, 58, alpha(200 * dustLife), false, true);
        VisualMesh.torus(pose, NUKE_FIRE, crater * 0.60f, 0.64f, 60, 11,
                255, 78, 14, alpha(172 * heatLife), true, false);
        VisualMesh.torus(pose, WHITE, crater * 0.82f, 0.28f, 60, 9,
                255, 224, 180, alpha(110 * heatLife), true, false);
        pose.popPose();

        if (hotA > 0) {
            pose.pushPose();
            pose.translate(0, 0.65f, 0);
            pose.scale(1.0f, 0.18f, 1.0f);
            VisualMesh.sphere(pose, NUKE_FIRE, crater * 0.52f, 18, 36,
                    255, 72, 10, alpha(hotA * 0.28f), true, false);
            pose.popPose();
        }
    }

    private static void renderImpactChunks(PoseStack pose, float time, float phase, int count, float spread,
                                           ResourceLocation rock, ResourceLocation glow,
                                           int rr, int rg, int rb, int gr, int gg, int gb, float speedScale) {
        float life = 1.0f - smooth(46.0f, 132.0f, time);
        if (life <= 0.01f) return;
        for (int i = 0; i < count; i++) {
            float a = phase + (float) (i * Math.PI * 2.0 / count);
            float speed = speedScale * (0.22f + (i % 7) * 0.038f);
            float travel = Math.min(time, 62.0f) * speed;
            float maxTravel = spread * (0.36f + (i % 6) * 0.078f);
            travel = Math.min(travel, maxTravel);
            float rise = travel * (0.48f + (i % 4) * 0.11f) - time * time * 0.0019f;
            pose.pushPose();
            pose.translate(Mth.cos(a) * travel, 1.2f + Math.max(0.0f, rise), Mth.sin(a) * travel);
            pose.mulPose(Axis.YP.rotationDegrees(time * (1.9f + i * 0.09f)));
            pose.mulPose(Axis.XP.rotationDegrees(i * 21.0f + time * 0.76f));
            pose.scale(0.52f + (i % 5) * 0.13f, 0.36f + (i % 4) * 0.11f, 0.49f + (i % 6) * 0.10f);
            VisualMesh.sphere(pose, rock, 1.0f, 9, 16, rr, rg, rb, alpha(228 * life), false, true);
            VisualMesh.sphere(pose, glow, 1.05f, 8, 15, gr, gg, gb, alpha(118 * life), true, false);
            pose.popPose();
        }
    }

    private static void renderShockRings(PoseStack pose, float time, float maxRadius,
                                         ResourceLocation texture, int red, int green, int blue,
                                         int maxAlpha, int count) {
        for (int i = 0; i < count; i++) {
            float radius = time * (0.84f + i * 0.16f) - i * 2.7f;
            if (radius <= 1.0f || radius >= maxRadius * 1.46f) continue;
            float life = 1.0f - radius / (maxRadius * 1.46f);
            pose.pushPose();
            pose.translate(0, 0.30f + i * 0.075f, 0);
            pose.scale(1.0f, 0.15f + i * 0.020f, 1.0f);
            VisualMesh.torus(pose, texture, radius, 0.54f + radius * 0.017f,
                    56, 9, red, green, blue, alpha(maxAlpha * life), true, false);
            pose.popPose();
        }
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
