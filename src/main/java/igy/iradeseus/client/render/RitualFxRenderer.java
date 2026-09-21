package igy.iradeseus.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import igy.iradeseus.IraDeSeus3D;
import igy.iradeseus.entity.RitualFxEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class RitualFxRenderer extends EntityRenderer<RitualFxEntity> {
    private static final ResourceLocation TEXTURE =
        new ResourceLocation(IraDeSeus3D.MOD_ID, "textures/fx/energy.png");

    public RitualFxRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(RitualFxEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float age = entity.tickCount + partialTicks;
        float life = Math.max(1.0F, entity.getLifetime());
        float progress = Mth.clamp(age / life, 0.0F, 1.0F);
        float pulse = 0.82F + Mth.sin(age * 0.13F) * 0.18F;
        float scale = entity.getFxScale() * pulse;
        float spin = age * entity.getSpin() * 1.8F;

        int color = entity.getColor();
        float r = ((color >> 16) & 255) / 255.0F;
        float g = ((color >> 8) & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        float alpha = 0.82F * (1.0F - Mth.clamp((progress - 0.78F) / 0.22F, 0.0F, 1.0F));

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        poseStack.scale(scale, scale, scale);

        switch (entity.getVariant()) {
            case 0 -> renderGroundSigil(vc, poseStack, spin, r, g, b, alpha);
            case 1 -> renderPortal(vc, poseStack, spin, r, g, b, alpha);
            case 2 -> renderCrown(vc, poseStack, spin, r, g, b, alpha);
            case 3 -> renderOrb(vc, poseStack, spin, r, g, b, alpha);
            case 4 -> renderCage(vc, poseStack, spin, r, g, b, alpha);
            case 5 -> renderShockwave(vc, poseStack, progress, spin, r, g, b, alpha);
            case 6 -> renderVortex(vc, poseStack, spin, r, g, b, alpha);
            case 7 -> renderEye(vc, poseStack, spin, r, g, b, alpha);
            default -> renderOrb(vc, poseStack, spin, r, g, b, alpha);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void renderGroundSigil(VertexConsumer vc, PoseStack ps, float spin,
                                          float r, float g, float b, float a) {
        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(spin));
        MythicMesh.ring(vc, ps.last(), 0.42F, 0.56F, 0.01F, 48, r,g,b,a,0);
        MythicMesh.ring(vc, ps.last(), 0.78F, 0.86F, 0.02F, 64, r,g,b,a * 0.85F,0.2F);
        MythicMesh.ring(vc, ps.last(), 1.05F, 1.11F, 0.03F, 72, r,g,b,a * 0.55F,0.45F);
        for (int i = 0; i < 8; i++) {
            ps.pushPose();
            ps.mulPose(Axis.YP.rotationDegrees(i * 45.0F));
            ps.translate(0.0F, 0.035F, 0.84F);
            MythicMesh.box(vc, ps.last(), 0.055F, 0.018F, 0.18F, r,g,b,a * 0.75F);
            ps.popPose();
        }
        ps.popPose();
    }

    private static void renderPortal(VertexConsumer vc, PoseStack ps, float spin,
                                     float r, float g, float b, float a) {
        ps.pushPose();
        ps.mulPose(Axis.ZP.rotationDegrees(spin * 0.22F));
        MythicMesh.verticalRing(vc, ps.last(), 0.58F, 0.78F, 64, r,g,b,a,0);
        MythicMesh.verticalRing(vc, ps.last(), 0.88F, 0.98F, 64, r,g,b,a * 0.64F,0.25F);
        ps.mulPose(Axis.YP.rotationDegrees(90.0F));
        MythicMesh.verticalRing(vc, ps.last(), 0.62F, 0.70F, 56, r,g,b,a * 0.45F,0.1F);
        ps.popPose();
    }

    private static void renderCrown(VertexConsumer vc, PoseStack ps, float spin,
                                    float r, float g, float b, float a) {
        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(spin));
        MythicMesh.ring(vc, ps.last(), 0.55F, 0.67F, -0.30F, 48, r,g,b,a,0);
        MythicMesh.ring(vc, ps.last(), 0.82F, 0.91F, 0.0F, 64, r,g,b,a * 0.78F,0.3F);
        MythicMesh.ring(vc, ps.last(), 1.06F, 1.13F, 0.30F, 72, r,g,b,a * 0.5F,0.6F);
        ps.popPose();
    }

    private static void renderOrb(VertexConsumer vc, PoseStack ps, float spin,
                                  float r, float g, float b, float a) {
        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(spin));
        MythicMesh.verticalRing(vc, ps.last(), 0.36F, 0.50F, 48, r,g,b,a,0);
        ps.mulPose(Axis.YP.rotationDegrees(60.0F));
        MythicMesh.verticalRing(vc, ps.last(), 0.46F, 0.57F, 48, r,g,b,a * 0.76F,0.15F);
        ps.mulPose(Axis.XP.rotationDegrees(90.0F));
        MythicMesh.ring(vc, ps.last(), 0.32F, 0.44F, 0.0F, 48, r,g,b,a * 0.72F,0.25F);
        ps.popPose();
    }

    private static void renderCage(VertexConsumer vc, PoseStack ps, float spin,
                                   float r, float g, float b, float a) {
        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(spin * 0.7F));
        MythicMesh.cylinder(vc, ps.last(), 0.88F, 2.2F, 40, r,g,b,a * 0.18F,0);
        MythicMesh.ring(vc, ps.last(), 0.82F, 0.94F, -1.05F, 56, r,g,b,a,0);
        MythicMesh.ring(vc, ps.last(), 0.82F, 0.94F, 1.05F, 56, r,g,b,a,0.2F);
        for (int i = 0; i < 10; i++) {
            ps.pushPose();
            ps.mulPose(Axis.YP.rotationDegrees(i * 36.0F));
            ps.translate(0.0F, -1.0F, 0.88F);
            MythicMesh.beam(vc, ps.last(), 0.025F, 2.0F, r,g,b,a * 0.78F);
            ps.popPose();
        }
        ps.popPose();
    }

    private static void renderShockwave(VertexConsumer vc, PoseStack ps, float progress, float spin,
                                        float r, float g, float b, float a) {
        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(spin));
        float radius = 0.25F + progress * 1.25F;
        MythicMesh.ring(vc, ps.last(), radius, radius + 0.12F, 0.0F, 72, r,g,b,a,0);
        MythicMesh.ring(vc, ps.last(), radius * 0.62F, radius * 0.62F + 0.07F,
            0.05F, 64, r,g,b,a * 0.55F,0.4F);
        ps.popPose();
    }

    private static void renderVortex(VertexConsumer vc, PoseStack ps, float spin,
                                     float r, float g, float b, float a) {
        ps.pushPose();
        ps.mulPose(Axis.YP.rotationDegrees(spin * 0.45F));
        for (int i = 0; i < 7; i++) {
            float y = -1.8F + i * 0.6F;
            float radius = 1.15F - Math.abs(i - 3) * 0.09F;
            MythicMesh.ring(vc, ps.last(), radius - 0.10F, radius, y, 64,
                r,g,b,a * (0.32F + i * 0.07F), i * 0.28F);
        }
        MythicMesh.cylinder(vc, ps.last(), 0.72F, 4.2F, 48, r,g,b,a * 0.14F,0);
        ps.popPose();
    }

    private static void renderEye(VertexConsumer vc, PoseStack ps, float spin,
                                  float r, float g, float b, float a) {
        ps.pushPose();
        ps.mulPose(Axis.ZP.rotationDegrees(spin * 0.25F));
        MythicMesh.verticalRing(vc, ps.last(), 0.72F, 0.92F, 64, r,g,b,a,0);
        MythicMesh.verticalRing(vc, ps.last(), 0.28F, 0.43F, 48, 1.0F,0.96F,0.72F,a,0.3F);
        ps.pushPose();
        ps.translate(0.0F, -4.5F, 0.0F);
        MythicMesh.beam(vc, ps.last(), 0.08F, 4.5F, r,g,b,a * 0.36F);
        ps.popPose();
        ps.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(RitualFxEntity entity) {
        return TEXTURE;
    }
}
