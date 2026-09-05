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
        float rise = smooth(0.0f, 108.0f, after);
        float capGrow = smooth(4.0f, 88.0f, after);
        float fade = 1.0f - smooth(Math.max(105.0f, duration - impact - 28.0f), duration - impact + 20.0f, after);
        int smokeA = alpha(246 * born * fade);
        int hotA = alpha(238 * born * (1.0f - smooth(38.0f, 118.0f, after)));
        float detailPhase = phase + (float) ((seed >>> 11) & 0xFFL) * 0.011f;

        renderInitialBlast(pose, after, born, hotA, detailPhase);

        float stemH = 8.5f + 34.0f * rise;
        renderStem(pose, after, stemH, rise, smokeA, hotA, detailPhase);

        float capY = 11.0f + 29.0f * rise;
        float capR = 5.8f + 20.0f * capGrow;
        renderMushroomCap(pose, after, capY, capR, smokeA, hotA, detailPhase);

        float crater = Math.max(22.0f, terrainRadius);
        renderGroundBlast(pose, after, crater, detailPhase);

        renderImpactChunks(pose, after, detailPhase, 28, crater * 0.82f, PLANET, PLANET_GLOW,
                148, 126, 105, 255, 91, 20, 1.10f);
        renderShockFront(pose, after, Math.max(crater * 1.34f, (float) damageRadius * 1.36f));
    }

    /** The physical missile model is intentionally unchanged; only the old surrounding glow rings were removed. */
    private static void renderMissile(PoseStack pose, float t, float impact, float phase) {
        float q = smooth(0.0f, impact, t);
        float y = Mth.lerp(q, 88.0f, 2.30f);
        float heat = smooth(0.0f, impact * 0.72f, t);
        float pulse = 0.93f + 0.07f * Mth.sin(t * 0.58f);
        float roll = t * 3.4f + phase * 11.0f;

        pose.pushPose();
        pose.translate(0, y, 0);
        pose.mulPose(Axis.YP.rotationDegrees(roll));

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

        pose.pushPose();
        VisualMesh.torus(pose, METAL, 0.93f, 0.13f, 42, 9, 205, 211, 218, 255, false, true);
        VisualMesh.cone(pose, METAL, 0.94f, -2.22f, 44, 108, 116, 128, 255, false, true);
        VisualMesh.cone(pose, WHITE, 0.25f, -2.30f, 30, 235, 242, 249, alpha(48 + heat * 82), true, false);
        pose.popPose();

        for (int i = 0; i < 4; i++) {
            pose.pushPose();
            pose.mulPose(Axis.YP.rotationDegrees(i * 90.0f));
            pose.translate(0.82f, 4.72f, 0);
            pose.mulPose(Axis.ZP.rotationDegrees(-90.0f));
            pose.scale(1.0f, 1.0f, 0.28f);
            VisualMesh.cone(pose, METAL, 0.78f, 1.65f, 28, 104, 112, 123, 255, false, true);
            pose.popPose();
        }

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

        pose.popPose();
    }

    private static void renderInitialBlast(PoseStack pose, float after, float born, int hotA, float phase) {
        float grow = smooth(0.0f, 25.0f, after);
        float coreLife = 1.0f - smooth(7.0f, 27.0f, after);
        float radius = 3.2f + 10.8f * grow;

        pose.pushPose();
        pose.translate(0, 1.25f, 0);
        VisualMesh.sphere(pose, NUKE_FIRE, radius * 1.12f, 22, 42,
                255, 73, 10, alpha(hotA * 0.34f), true, false);
        VisualMesh.sphere(pose, NUKE_FIRE, radius * 0.76f, 24, 46,
                255, 151, 42, alpha(hotA * 0.72f), true, false);
        if (coreLife > 0.01f) {
            VisualMesh.sphere(pose, WHITE, radius * 0.31f, 18, 34,
                    255, 247, 224, alpha(205 * born * coreLife), true, false);
        }
        pose.popPose();

        float lobeLife = 1.0f - smooth(30.0f, 72.0f, after);
        if (lobeLife > 0.01f) {
            for (int i = 0; i < 14; i++) {
                float a = phase + (float) (i * Math.PI * 2.0 / 14.0) + after * 0.004f;
                float radial = radius * (0.28f + (i % 4) * 0.10f);
                float y = 1.0f + radius * (0.05f + (i % 5) * 0.055f);
                float blob = radius * (0.24f + (i % 3) * 0.035f);
                pose.pushPose();
                pose.translate(Mth.cos(a) * radial, y, Mth.sin(a) * radial);
                pose.scale(1.22f, 0.92f + (i % 3) * 0.10f, 1.22f);
                VisualMesh.sphere(pose, NUKE_FIRE, blob, 12, 22,
                        255, 92 + (i % 3) * 25, 12, alpha(180 * born * lobeLife), true, false);
                pose.popPose();
            }
        }
    }

    private static void renderStem(PoseStack pose, float after, float stemH, float rise,
                                   int smokeA, int hotA, float phase) {
        pose.pushPose();
        pose.translate(0, 0.42f, 0);
        VisualMesh.cylinder(pose, NUKE_SMOKE, 4.05f + 2.20f * rise, stemH, 44,
                132, 127, 124, smokeA, false, false);
        VisualMesh.cylinder(pose, NUKE_SMOKE, 2.70f + 1.65f * rise, stemH * 0.96f, 40,
                204, 188, 166, alpha(smokeA * 0.60f), false, false);
        VisualMesh.cylinder(pose, NUKE_FIRE, 1.55f + 0.92f * rise, Math.min(stemH, 15.5f), 36,
                255, 98, 16, alpha(hotA * 0.76f), true, false);
        pose.popPose();

        for (int i = 0; i < 14; i++) {
            float h = stemH * (0.08f + i * 0.064f);
            float a = phase + i * 1.83f + after * (0.010f + (i % 3) * 0.002f);
            float ring = 1.7f + rise * 2.65f + (i % 4) * 0.40f;
            float blob = 2.25f + rise * 1.48f + (i % 3) * 0.34f;
            pose.pushPose();
            pose.translate(Mth.cos(a) * ring, h, Mth.sin(a) * ring);
            pose.scale(1.15f, 0.84f + (i % 4) * 0.07f, 1.15f);
            VisualMesh.sphere(pose, NUKE_SMOKE, blob, 13, 24,
                    142 + (i % 3) * 13, 135 + (i % 3) * 11, 126 + (i % 3) * 9,
                    alpha(smokeA * 0.80f), false, false);
            pose.popPose();
        }
    }

    private static void renderMushroomCap(PoseStack pose, float after, float capY, float capR,
                                          int smokeA, int hotA, float phase) {
        for (int i = 0; i < 38; i++) {
            float a = (float) (i * Math.PI * 2.0 / 38.0 + phase * 0.43 + after * (0.0032 + (i % 5) * 0.0006));
            float ring = capR * (0.14f + 0.041f * (i % 11));
            float ox = Mth.cos(a) * ring;
            float oz = Mth.sin(a) * ring;
            float oy = capY + ((i % 8) - 3.5f) * capR * 0.068f;
            float blob = capR * (0.205f + (i % 6) * 0.027f);
            pose.pushPose();
            pose.translate(ox, oy, oz);
            pose.mulPose(Axis.YP.rotationDegrees(after * (0.19f + i * 0.014f)));
            pose.scale(1.34f, 0.68f + (i % 4) * 0.075f, 1.34f);
            VisualMesh.sphere(pose, NUKE_SMOKE, blob, 15, 28,
                    151 + (i % 4) * 11, 143 + (i % 4) * 10, 135 + (i % 4) * 9,
                    alpha(smokeA * (0.78f + (i % 4) * 0.045f)), false, false);
            pose.popPose();
        }

        pose.pushPose();
        pose.translate(0, capY, 0);
        pose.scale(1.10f, 0.56f, 1.10f);
        VisualMesh.sphere(pose, NUKE_SMOKE, capR * 0.94f, 20, 40,
                170, 159, 146, alpha(smokeA * 0.80f), false, false);
        VisualMesh.sphere(pose, NUKE_FIRE, capR * 0.62f, 18, 36,
                255, 82, 12, alpha(hotA * 0.32f), true, false);
        pose.popPose();

        float hotLife = 1.0f - smooth(36.0f, 105.0f, after);
        if (hotLife > 0.01f) {
            for (int i = 0; i < 14; i++) {
                float a = phase + (float) (i * Math.PI * 2.0 / 14.0) + after * 0.007f;
                float rr = capR * (0.30f + (i % 4) * 0.075f);
                pose.pushPose();
                pose.translate(Mth.cos(a) * rr, capY - capR * (0.16f + (i % 3) * 0.035f), Mth.sin(a) * rr);
                pose.scale(1.20f, 0.68f, 1.20f);
                VisualMesh.sphere(pose, NUKE_FIRE, capR * (0.13f + (i % 3) * 0.024f), 12, 22,
                        255, 112, 20, alpha(118 * hotLife), true, false);
                pose.popPose();
            }
        }
    }

    private static void renderGroundBlast(PoseStack pose, float after, float crater, float phase) {
        float dustLife = 1.0f - smooth(56.0f, 155.0f, after);
        float expand = smooth(0.0f, 36.0f, after);

        pose.pushPose();
        pose.translate(0, 0.12f, 0);
        VisualMesh.annulus(pose, PLANET,
                crater * (0.18f + 0.30f * expand), crater * (0.58f + 0.43f * expand), 76,
                81, 69, 60, alpha(148 * dustLife), false, true);
        pose.popPose();

        float groundFireLife = 1.0f - smooth(24.0f, 70.0f, after);
        if (groundFireLife > 0.01f) {
            for (int i = 0; i < 12; i++) {
                float a = phase + (float) (i * Math.PI * 2.0 / 12.0);
                float rr = crater * (0.08f + expand * (0.18f + (i % 4) * 0.035f));
                pose.pushPose();
                pose.translate(Mth.cos(a) * rr, 0.45f + (i % 3) * 0.22f, Mth.sin(a) * rr);
                pose.scale(1.45f, 0.42f, 1.45f);
                VisualMesh.sphere(pose, NUKE_FIRE, crater * (0.055f + (i % 3) * 0.012f), 11, 20,
                        255, 84, 12, alpha(118 * groundFireLife), true, false);
                pose.popPose();
            }
        }
    }

    private static void renderImpactChunks(PoseStack pose, float time, float phase, int count, float spread,
                                           ResourceLocation rock, ResourceLocation glow,
                                           int rr, int rg, int rb, int gr, int gg, int gb, float speedScale) {
        float life = 1.0f - smooth(42.0f, 125.0f, time);
        if (life <= 0.01f) return;
        for (int i = 0; i < count; i++) {
            float a = phase + (float) (i * Math.PI * 2.0 / count);
            float speed = speedScale * (0.22f + (i % 7) * 0.038f);
            float travel = Math.min(time, 60.0f) * speed;
            float maxTravel = spread * (0.36f + (i % 6) * 0.075f);
            travel = Math.min(travel, maxTravel);
            float rise = travel * (0.46f + (i % 4) * 0.11f) - time * time * 0.0019f;
            pose.pushPose();
            pose.translate(Mth.cos(a) * travel, 1.2f + Math.max(0.0f, rise), Mth.sin(a) * travel);
            pose.mulPose(Axis.YP.rotationDegrees(time * (1.8f + i * 0.09f)));
            pose.mulPose(Axis.XP.rotationDegrees(i * 21.0f + time * 0.72f));
            pose.scale(0.50f + (i % 5) * 0.12f, 0.34f + (i % 4) * 0.10f, 0.47f + (i % 6) * 0.09f);
            VisualMesh.sphere(pose, rock, 1.0f, 9, 16, rr, rg, rb, alpha(218 * life), false, true);
            VisualMesh.sphere(pose, glow, 1.04f, 8, 15, gr, gg, gb, alpha(78 * life), true, false);
            pose.popPose();
        }
    }

    /** One restrained pressure front instead of a stack of bright yellow rings. */
    private static void renderShockFront(PoseStack pose, float time, float maxRadius) {
        float radius = time * 1.18f;
        if (radius <= 2.0f || radius >= maxRadius * 1.32f) return;
        float life = 1.0f - radius / (maxRadius * 1.32f);
        pose.pushPose();
        pose.translate(0, 0.34f, 0);
        pose.scale(1.0f, 0.11f, 1.0f);
        VisualMesh.torus(pose, NUKE_SMOKE, radius, 0.78f + radius * 0.018f,
                64, 10, 205, 194, 180, alpha(88 * life), false, false);
        pose.popPose();
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
