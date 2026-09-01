package com.igy.nuke3d.client;

import com.igy.nuke3d.Nuke3D;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Exact high-detail NUKE renderer from DESASTRE-3D 3.2.1, isolated from all other disasters. */
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
            float q = smooth(0.0f, impact, t);
            float y = Mth.lerp(q, 86.0f, 2.3f);
            float heat = smooth(0.0f, impact * 0.72f, t);
            float pulse = 0.93f + 0.07f * Mth.sin(t * 0.58f);

            pose.pushPose();
            pose.translate(0, y, 0);
            pose.mulPose(Axis.YP.rotationDegrees(t * 3.8f));

            pose.pushPose();
            pose.translate(0, 2.10f, 0);
            VisualMesh.cylinder(pose, METAL, 0.96f, 5.85f, 40, 155, 162, 170, 255, false, true);
            VisualMesh.cylinder(pose, METAL, 0.72f, 5.90f, 36, 91, 98, 108, 255, false, true);
            pose.popPose();

            pose.pushPose();
            pose.translate(0, 2.12f, 0);
            VisualMesh.torus(pose, METAL, 0.92f, 0.13f, 40, 8, 202, 208, 214, 255, false, true);
            pose.mulPose(Axis.XP.rotationDegrees(180.0f));
            VisualMesh.cone(pose, METAL, 0.95f, 2.55f, 40, 105, 111, 120, 255, false, true);
            VisualMesh.cone(pose, WHITE, 0.25f, 2.63f, 28, 236, 241, 246, alpha(58 + heat * 60), true, false);
            pose.popPose();

            pose.pushPose();
            pose.translate(0, 7.92f, 0);
            VisualMesh.torus(pose, METAL, 1.05f, 0.17f, 40, 9, 79, 86, 96, 255, false, true);
            VisualMesh.torus(pose, METAL, 0.78f, 0.10f, 36, 8, 198, 204, 211, 255, false, true);
            for (int i = 0; i < 4; i++) {
                pose.pushPose();
                pose.mulPose(Axis.YP.rotationDegrees(i * 90.0f));
                pose.translate(1.12f, -0.85f, 0);
                pose.scale(0.52f, 1.42f, 0.18f);
                VisualMesh.sphere(pose, METAL, 0.98f, 10, 18, 100, 108, 119, 255, false, true);
                pose.popPose();
            }
            VisualMesh.cone(pose, NUKE_FIRE, 0.70f * pulse, 4.85f + 1.15f * Mth.sin(t * 0.63f), 30,
                    255, 112, 20, 232, true, false);
            VisualMesh.cone(pose, WHITE, 0.27f * pulse, 3.65f, 26,
                    255, 244, 214, 205, true, false);
            pose.popPose();

            for (int i = 0; i < 3; i++) {
                pose.pushPose();
                pose.translate(0, 2.4f + i * 1.35f, 0);
                pose.mulPose(Axis.YP.rotationDegrees((-1.0f + i * 0.35f) * t * 8.0f));
                pose.mulPose(Axis.XP.rotationDegrees(52.0f + i * 13.0f));
                VisualMesh.torus(pose, i == 2 ? WHITE : NUKE_FIRE,
                        1.42f + heat * 0.38f + i * 0.22f, 0.08f + i * 0.025f, 42, 8,
                        255, 125 + i * 38, 28 + i * 45, alpha((92 + i * 28) * heat), true, false);
                pose.popPose();
            }
            pose.popPose();
            return;
        }

        float after = t - impact;
        float born = smooth(0.0f, 9.0f, after);
        float rise = smooth(0.0f, 98.0f, after);
        float capGrow = smooth(0.0f, 78.0f, after);
        float fade = 1.0f - smooth(Math.max(95.0f, duration - impact - 30.0f), duration - impact + 18.0f, after);
        int smokeA = alpha(248 * born * fade);
        int hotA = alpha(250 * born * (1.0f - smooth(40.0f, 126.0f, after)));

        pose.pushPose();
        pose.translate(0, 1.6f, 0);
        float flash = 3.8f + 9.8f * smooth(0.0f, 25.0f, after);
        VisualMesh.sphere(pose, NUKE_FIRE, flash * 1.30f, 22, 42, 255, 72, 12, alpha(hotA * 0.72f), true, false);
        VisualMesh.sphere(pose, NUKE_FIRE, flash, 24, 46, 255, 145, 40, hotA, true, false);
        VisualMesh.sphere(pose, WHITE, flash * 0.52f, 18, 36, 255, 250, 226, alpha(hotA * 0.90f), true, false);
        pose.popPose();

        float stemH = 8.0f + 31.0f * rise;
        pose.pushPose();
        pose.translate(0, 0.45f, 0);
        VisualMesh.cylinder(pose, NUKE_SMOKE, 3.9f + 2.25f * rise, stemH, 42,
                145, 138, 132, smokeA, false, false);
        VisualMesh.cylinder(pose, NUKE_SMOKE, 2.75f + 1.65f * rise, stemH * 0.94f, 38,
                205, 187, 162, alpha(smokeA * 0.67f), false, false);
        VisualMesh.cylinder(pose, NUKE_FIRE, 1.72f + 0.95f * rise, Math.min(stemH, 16.0f), 34,
                255, 116, 24, hotA, true, false);
        pose.popPose();

        float capY = 10.0f + 27.0f * rise;
        float capR = 5.8f + 18.8f * capGrow;

        for (int i = 0; i < 30; i++) {
            float a = (float) (i * Math.PI * 2.0 / 30.0 + phase * 0.43 + t * (0.0038 + (i % 5) * 0.0007));
            float ring = capR * (0.17f + 0.047f * (i % 9));
            float ox = Mth.cos(a) * ring;
            float oz = Mth.sin(a) * ring;
            float oy = capY + ((i % 6) - 2.5f) * capR * 0.075f;
            float blob = capR * (0.23f + (i % 6) * 0.027f);
            pose.pushPose();
            pose.translate(ox, oy, oz);
            pose.mulPose(Axis.YP.rotationDegrees(t * (0.22f + i * 0.018f)));
            pose.scale(1.35f, 0.61f + (i % 4) * 0.075f, 1.35f);
            VisualMesh.sphere(pose, NUKE_SMOKE, blob, 15, 28,
                    165 + (i % 4) * 9, 154 + (i % 4) * 8, 142 + (i % 4) * 7,
                    alpha(smokeA * (0.83f + (i % 4) * 0.045f)), false, false);
            pose.popPose();
        }

        pose.pushPose();
        pose.translate(0, capY - capR * 0.10f, 0);
        pose.scale(1.0f, 0.38f, 1.0f);
        VisualMesh.sphere(pose, NUKE_FIRE, capR * 1.10f, 18, 36, 255, 88, 14, alpha(hotA * 0.86f), true, false);
        VisualMesh.sphere(pose, WHITE, capR * 0.48f, 16, 32, 255, 235, 190, alpha(hotA * 0.56f), true, false);
        pose.popPose();

        float crater = Math.max(22.0f, terrainRadius);
        pose.pushPose();
        pose.translate(0, 0.16f, 0);
        VisualMesh.annulus(pose, PLANET, crater * 0.48f, crater * 1.03f, 72,
                83, 70, 60, alpha(190 * (1.0f - smooth(55.0f, 145.0f, after))), false, true);
        VisualMesh.torus(pose, NUKE_FIRE, crater * 0.62f, 0.55f, 56, 10,
                255, 88, 18, alpha(150 * (1.0f - smooth(28.0f, 92.0f, after))), true, false);
        pose.popPose();

        renderImpactChunks(pose, after, phase, 26, crater * 0.78f, PLANET, PLANET_GLOW,
                148, 126, 105, 255, 91, 20, 1.08f);
        renderShockRings(pose, after, Math.max(crater * 1.22f, (float) damageRadius * 1.25f), NUKE_FIRE,
                255, 188, 112, 238, 5);
    }

    private static void renderImpactChunks(PoseStack pose, float time, float phase, int count, float spread,
                                           ResourceLocation rock, ResourceLocation glow,
                                           int rr, int rg, int rb, int gr, int gg, int gb, float speedScale) {
        float life = 1.0f - smooth(42.0f, 125.0f, time);
        if (life <= 0.01f) return;
        for (int i = 0; i < count; i++) {
            float a = phase + (float) (i * Math.PI * 2.0 / count);
            float speed = speedScale * (0.22f + (i % 7) * 0.038f);
            float travel = Math.min(time, 58.0f) * speed;
            float maxTravel = spread * (0.36f + (i % 6) * 0.075f);
            travel = Math.min(travel, maxTravel);
            float rise = travel * (0.46f + (i % 4) * 0.11f) - time * time * 0.0018f;
            pose.pushPose();
            pose.translate(Mth.cos(a) * travel, 1.2f + Math.max(0.0f, rise), Mth.sin(a) * travel);
            pose.mulPose(Axis.YP.rotationDegrees(time * (1.8f + i * 0.09f)));
            pose.mulPose(Axis.XP.rotationDegrees(i * 21.0f + time * 0.72f));
            pose.scale(0.52f + (i % 5) * 0.13f, 0.36f + (i % 4) * 0.11f, 0.49f + (i % 6) * 0.10f);
            VisualMesh.sphere(pose, rock, 1.0f, 9, 16, rr, rg, rb, alpha(225 * life), false, true);
            VisualMesh.sphere(pose, glow, 1.04f, 8, 15, gr, gg, gb, alpha(105 * life), true, false);
            pose.popPose();
        }
    }

    private static void renderShockRings(PoseStack pose, float time, float maxRadius,
                                         ResourceLocation texture, int red, int green, int blue,
                                         int maxAlpha, int count) {
        for (int i = 0; i < count; i++) {
            float radius = time * (0.82f + i * 0.16f) - i * 2.8f;
            if (radius <= 1.0f || radius >= maxRadius * 1.42f) continue;
            float life = 1.0f - radius / (maxRadius * 1.42f);
            pose.pushPose();
            pose.translate(0, 0.30f + i * 0.075f, 0);
            pose.scale(1.0f, 0.17f + i * 0.021f, 1.0f);
            VisualMesh.torus(pose, texture, radius, 0.52f + radius * 0.017f,
                    52, 8, red, green, blue, alpha(maxAlpha * life), true, false);
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
