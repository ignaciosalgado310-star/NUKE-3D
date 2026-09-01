package com.igy.nuke3d.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/** Procedural textured 3D meshes copied from DESASTRE-3D 3.2.1. */
public final class VisualMesh {
    private static final float TAU = (float) (Math.PI * 2.0);
    private static final float LIGHT_X = -0.34f;
    private static final float LIGHT_Y = 0.76f;
    private static final float LIGHT_Z = 0.55f;

    private VisualMesh() {}

    public static void sphere(PoseStack pose, ResourceLocation texture, float radius,
                              int latSteps, int lonSteps,
                              int red, int green, int blue, int alpha,
                              boolean additive, boolean depthWrite) {
        if (radius <= 0.001f || !setup(texture, additive, depthWrite)) return;
        Matrix4f matrix = pose.last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        int lats = Math.max(6, latSteps);
        int lons = Math.max(10, lonSteps);
        for (int i = 0; i < lats; i++) {
            float v0 = i / (float) lats;
            float v1 = (i + 1) / (float) lats;
            float theta0 = (v0 - 0.5f) * Mth.PI;
            float theta1 = (v1 - 0.5f) * Mth.PI;
            float c0 = Mth.cos(theta0);
            float c1 = Mth.cos(theta1);
            float s0 = Mth.sin(theta0);
            float s1 = Mth.sin(theta1);
            float y0 = s0 * radius;
            float y1 = s1 * radius;

            for (int j = 0; j < lons; j++) {
                float u0 = j / (float) lons;
                float u1 = (j + 1) / (float) lons;
                float phi0 = u0 * TAU;
                float phi1 = u1 * TAU;
                float cp0 = Mth.cos(phi0), sp0 = Mth.sin(phi0);
                float cp1 = Mth.cos(phi1), sp1 = Mth.sin(phi1);

                float x00 = c0 * cp0 * radius;
                float z00 = c0 * sp0 * radius;
                float x01 = c0 * cp1 * radius;
                float z01 = c0 * sp1 * radius;
                float x11 = c1 * cp1 * radius;
                float z11 = c1 * sp1 * radius;
                float x10 = c1 * cp0 * radius;
                float z10 = c1 * sp0 * radius;

                vertexLit(builder, matrix, x00, y0, z00, u0, 1.0f - v0, c0 * cp0, s0, c0 * sp0, red, green, blue, alpha, additive);
                vertexLit(builder, matrix, x01, y0, z01, u1, 1.0f - v0, c0 * cp1, s0, c0 * sp1, red, green, blue, alpha, additive);
                vertexLit(builder, matrix, x11, y1, z11, u1, 1.0f - v1, c1 * cp1, s1, c1 * sp1, red, green, blue, alpha, additive);
                vertexLit(builder, matrix, x10, y1, z10, u0, 1.0f - v1, c1 * cp0, s1, c1 * sp0, red, green, blue, alpha, additive);
            }
        }
        BufferUploader.drawWithShader(builder.end());
    }

    public static void torus(PoseStack pose, ResourceLocation texture,
                             float majorRadius, float minorRadius,
                             int majorSteps, int minorSteps,
                             int red, int green, int blue, int alpha,
                             boolean additive, boolean depthWrite) {
        if (majorRadius <= 0.001f || minorRadius <= 0.001f || !setup(texture, additive, depthWrite)) return;
        Matrix4f matrix = pose.last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        int majors = Math.max(12, majorSteps);
        int minors = Math.max(6, minorSteps);
        for (int i = 0; i < majors; i++) {
            float u0 = i / (float) majors;
            float u1 = (i + 1) / (float) majors;
            float a0 = u0 * TAU;
            float a1 = u1 * TAU;
            for (int j = 0; j < minors; j++) {
                float v0 = j / (float) minors;
                float v1 = (j + 1) / (float) minors;
                float b0 = v0 * TAU;
                float b1 = v1 * TAU;

                Point p00 = torusPoint(majorRadius, minorRadius, a0, b0);
                Point p10 = torusPoint(majorRadius, minorRadius, a1, b0);
                Point p11 = torusPoint(majorRadius, minorRadius, a1, b1);
                Point p01 = torusPoint(majorRadius, minorRadius, a0, b1);

                vertexLit(builder, matrix, p00.x, p00.y, p00.z, u0, v0, Mth.cos(b0) * Mth.cos(a0), Mth.sin(b0), Mth.cos(b0) * Mth.sin(a0), red, green, blue, alpha, additive);
                vertexLit(builder, matrix, p10.x, p10.y, p10.z, u1, v0, Mth.cos(b0) * Mth.cos(a1), Mth.sin(b0), Mth.cos(b0) * Mth.sin(a1), red, green, blue, alpha, additive);
                vertexLit(builder, matrix, p11.x, p11.y, p11.z, u1, v1, Mth.cos(b1) * Mth.cos(a1), Mth.sin(b1), Mth.cos(b1) * Mth.sin(a1), red, green, blue, alpha, additive);
                vertexLit(builder, matrix, p01.x, p01.y, p01.z, u0, v1, Mth.cos(b1) * Mth.cos(a0), Mth.sin(b1), Mth.cos(b1) * Mth.sin(a0), red, green, blue, alpha, additive);
            }
        }
        BufferUploader.drawWithShader(builder.end());
    }

    public static void annulus(PoseStack pose, ResourceLocation texture,
                               float innerRadius, float outerRadius, int segments,
                               int red, int green, int blue, int alpha,
                               boolean additive, boolean depthWrite) {
        if (outerRadius <= innerRadius || !setup(texture, additive, depthWrite)) return;
        Matrix4f matrix = pose.last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        int steps = Math.max(16, segments);
        for (int i = 0; i < steps; i++) {
            float u0 = i / (float) steps;
            float u1 = (i + 1) / (float) steps;
            float a0 = u0 * TAU;
            float a1 = u1 * TAU;
            float c0 = Mth.cos(a0), s0 = Mth.sin(a0);
            float c1 = Mth.cos(a1), s1 = Mth.sin(a1);
            float glow0 = 0.82f + 0.18f * Math.max(0.0f, c0 * LIGHT_X + s0 * LIGHT_Z);
            float glow1 = 0.82f + 0.18f * Math.max(0.0f, c1 * LIGHT_X + s1 * LIGHT_Z);

            vertexTint(builder, matrix, c0 * innerRadius, 0, s0 * innerRadius, u0, 0, red, green, blue, alpha, glow0);
            vertexTint(builder, matrix, c1 * innerRadius, 0, s1 * innerRadius, u1, 0, red, green, blue, alpha, glow1);
            vertexTint(builder, matrix, c1 * outerRadius, 0, s1 * outerRadius, u1, 1, red, green, blue, alpha, glow1 * 0.92f);
            vertexTint(builder, matrix, c0 * outerRadius, 0, s0 * outerRadius, u0, 1, red, green, blue, alpha, glow0 * 0.92f);
        }
        BufferUploader.drawWithShader(builder.end());
    }

    public static void cylinder(PoseStack pose, ResourceLocation texture,
                                float radius, float height, int segments,
                                int red, int green, int blue, int alpha,
                                boolean additive, boolean depthWrite) {
        if (radius <= 0.001f || Math.abs(height) <= 0.001f || !setup(texture, additive, depthWrite)) return;
        Matrix4f matrix = pose.last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        int steps = Math.max(10, segments);
        for (int i = 0; i < steps; i++) {
            float u0 = i / (float) steps;
            float u1 = (i + 1) / (float) steps;
            float a0 = u0 * TAU;
            float a1 = u1 * TAU;
            float c0 = Mth.cos(a0), s0 = Mth.sin(a0);
            float c1 = Mth.cos(a1), s1 = Mth.sin(a1);
            float x0 = c0 * radius;
            float z0 = s0 * radius;
            float x1 = c1 * radius;
            float z1 = s1 * radius;
            vertexLit(builder, matrix, x0, 0, z0, u0, 1, c0, 0, s0, red, green, blue, alpha, additive);
            vertexLit(builder, matrix, x1, 0, z1, u1, 1, c1, 0, s1, red, green, blue, alpha, additive);
            vertexLit(builder, matrix, x1, height, z1, u1, 0, c1, 0, s1, red, green, blue, alpha, additive);
            vertexLit(builder, matrix, x0, height, z0, u0, 0, c0, 0, s0, red, green, blue, alpha, additive);
        }
        BufferUploader.drawWithShader(builder.end());
    }

    public static void cone(PoseStack pose, ResourceLocation texture,
                            float baseRadius, float height, int segments,
                            int red, int green, int blue, int alpha,
                            boolean additive, boolean depthWrite) {
        if (baseRadius <= 0.001f || Math.abs(height) <= 0.001f || !setup(texture, additive, depthWrite)) return;
        Matrix4f matrix = pose.last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_TEX_COLOR);
        int steps = Math.max(10, segments);
        float ny = baseRadius / Math.max(0.001f, Math.abs(height));
        for (int i = 0; i < steps; i++) {
            float u0 = i / (float) steps;
            float u1 = (i + 1) / (float) steps;
            float a0 = u0 * TAU;
            float a1 = u1 * TAU;
            float c0 = Mth.cos(a0), s0 = Mth.sin(a0);
            float c1 = Mth.cos(a1), s1 = Mth.sin(a1);
            float x0 = c0 * baseRadius;
            float z0 = s0 * baseRadius;
            float x1 = c1 * baseRadius;
            float z1 = s1 * baseRadius;
            float cm = Mth.cos((a0 + a1) * 0.5f), sm = Mth.sin((a0 + a1) * 0.5f);
            vertexLit(builder, matrix, 0, height, 0, (u0 + u1) * 0.5f, 0, cm, ny, sm, red, green, blue, alpha, additive);
            vertexLit(builder, matrix, x0, 0, z0, u0, 1, c0, ny, s0, red, green, blue, alpha, additive);
            vertexLit(builder, matrix, x1, 0, z1, u1, 1, c1, ny, s1, red, green, blue, alpha, additive);
        }
        BufferUploader.drawWithShader(builder.end());
    }

    public static void cone(PoseStack pose, ResourceLocation texture,
                            float baseRadius, float height, int segments,
                            int red, int green, int blue, float alpha,
                            boolean additive, boolean depthWrite) {
        cone(pose, texture, baseRadius, height, segments, red, green, blue,
                Mth.clamp((int) alpha, 0, 255), additive, depthWrite);
    }

    public static void restoreState() {
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    private static boolean setup(ResourceLocation texture, boolean additive, boolean depthWrite) {
        ShaderInstance shader = GameRenderer.getPositionTexColorShader();
        if (shader == null) return false;

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        if (additive) {
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ONE
            );
        } else {
            RenderSystem.defaultBlendFunc();
        }
        RenderSystem.depthMask(depthWrite);
        RenderSystem.disableCull();
        RenderSystem.setShader(() -> shader);
        RenderSystem.setShaderTexture(0, texture);
        return true;
    }

    private static Point torusPoint(float major, float minor, float a, float b) {
        float ring = major + minor * Mth.cos(b);
        return new Point(ring * Mth.cos(a), minor * Mth.sin(b), ring * Mth.sin(a));
    }

    private static void vertexLit(BufferBuilder builder, Matrix4f matrix,
                                  float x, float y, float z, float u, float v,
                                  float nx, float ny, float nz,
                                  int red, int green, int blue, int alpha,
                                  boolean emissive) {
        float len = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 0.0001f) {
            nx /= len;
            ny /= len;
            nz /= len;
        }
        float ndotl = Math.max(0.0f, nx * LIGHT_X + ny * LIGHT_Y + nz * LIGHT_Z);
        float rim = 1.0f - Math.abs(ny);
        float shade = emissive
                ? 0.90f + ndotl * 0.10f + rim * 0.05f
                : 0.48f + ndotl * 0.47f + rim * 0.07f;
        vertexTint(builder, matrix, x, y, z, u, v, red, green, blue, alpha, Mth.clamp(shade, 0.42f, 1.08f));
    }

    private static void vertexTint(BufferBuilder builder, Matrix4f matrix,
                                   float x, float y, float z, float u, float v,
                                   int red, int green, int blue, int alpha, float shade) {
        builder.vertex(matrix, x, y, z)
                .uv(u, v)
                .color(shade(red, shade), shade(green, shade), shade(blue, shade), clamp(alpha))
                .endVertex();
    }

    private static int shade(int value, float factor) {
        return clamp(Math.round(value * factor));
    }

    private static int clamp(int value) {
        return Mth.clamp(value, 0, 255);
    }

    private record Point(float x, float y, float z) {}
}
