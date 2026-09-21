package igy.iradeseus.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import igy.iradeseus.IraDeSeus3D;
import igy.iradeseus.entity.DivineProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class DivineProjectileRenderer extends EntityRenderer<DivineProjectileEntity> {
    private static final ResourceLocation TEXTURE =
        new ResourceLocation(IraDeSeus3D.MOD_ID, "textures/fx/divine_metal.png");

    public DivineProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(DivineProjectileEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        Vec3 motion = entity.getDeltaMovement();
        if (motion.lengthSqr() > 0.0001) {
            float yaw = (float) (Mth.atan2(motion.x, motion.z) * 180.0 / Math.PI);
            float horizontal = (float) Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            float pitch = (float) (Mth.atan2(motion.y, horizontal) * 180.0 / Math.PI);
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(-pitch + 90.0F));
        }

        float s = entity.getModelScale();
        poseStack.scale(s, s, s);
        poseStack.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTicks) * 9.0F));

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        float[] c = color(entity.getVariant());

        switch (entity.getVariant()) {
            case 0 -> renderZeus(vc, poseStack, c);
            case 1 -> renderThor(vc, poseStack, c);
            case 2 -> renderPoseidon(vc, poseStack, c);
            case 3 -> renderRa(vc, poseStack, c);
            case 4 -> renderHades(vc, poseStack, c);
            default -> renderZeus(vc, poseStack, c);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void renderZeus(VertexConsumer vc, PoseStack ps, float[] c) {
        MythicMesh.beam(vc, ps.last(), 0.10F, 2.4F, c[0],c[1],c[2],0.95F);
        ps.pushPose();
        ps.translate(0.22F, 0.7F, 0.0F);
        ps.mulPose(Axis.ZP.rotationDegrees(-38.0F));
        MythicMesh.beam(vc, ps.last(), 0.08F, 0.85F, 1.0F,1.0F,1.0F,0.95F);
        ps.popPose();
        ps.pushPose();
        ps.translate(-0.25F, 1.45F, 0.0F);
        ps.mulPose(Axis.ZP.rotationDegrees(42.0F));
        MythicMesh.beam(vc, ps.last(), 0.07F, 0.75F, 1.0F,1.0F,1.0F,0.9F);
        ps.popPose();
    }

    private static void renderThor(VertexConsumer vc, PoseStack ps, float[] c) {
        MythicMesh.beam(vc, ps.last(), 0.09F, 1.75F, c[0],c[1],c[2],0.9F);
        ps.pushPose();
        ps.translate(0.0F, 1.85F, 0.0F);
        MythicMesh.box(vc, ps.last(), 0.62F, 0.36F, 0.42F, c[0],c[1],c[2],1.0F);
        ps.translate(0.0F, 0.18F, 0.0F);
        MythicMesh.box(vc, ps.last(), 0.48F, 0.16F, 0.58F, 0.78F,0.94F,1.0F,0.75F);
        ps.popPose();
    }

    private static void renderPoseidon(VertexConsumer vc, PoseStack ps, float[] c) {
        MythicMesh.beam(vc, ps.last(), 0.07F, 2.5F, c[0],c[1],c[2],0.94F);
        for (int i = -1; i <= 1; i++) {
            ps.pushPose();
            ps.translate(i * 0.34F, 2.25F, 0.0F);
            MythicMesh.beam(vc, ps.last(), 0.055F, i == 0 ? 0.95F : 0.72F,
                0.55F,0.98F,1.0F,0.95F);
            ps.popPose();
        }
        ps.pushPose();
        ps.translate(0.0F, 2.23F, 0.0F);
        MythicMesh.box(vc, ps.last(), 0.52F, 0.06F, 0.08F, c[0],c[1],c[2],0.9F);
        ps.popPose();
    }

    private static void renderRa(VertexConsumer vc, PoseStack ps, float[] c) {
        MythicMesh.beam(vc, ps.last(), 0.08F, 2.35F, c[0],c[1],c[2],0.92F);
        ps.pushPose();
        ps.translate(0.0F, 2.35F, 0.0F);
        MythicMesh.ring(vc, ps.last(), 0.32F, 0.48F, 0.0F, 40, 1.0F,0.76F,0.18F,0.95F,0);
        ps.mulPose(Axis.XP.rotationDegrees(90.0F));
        MythicMesh.verticalRing(vc, ps.last(), 0.22F, 0.36F, 36, 1.0F,0.94F,0.55F,0.8F,0.1F);
        ps.popPose();
    }

    private static void renderHades(VertexConsumer vc, PoseStack ps, float[] c) {
        MythicMesh.beam(vc, ps.last(), 0.075F, 2.55F, c[0],c[1],c[2],0.93F);
        ps.pushPose();
        ps.translate(0.0F, 2.45F, 0.0F);
        ps.mulPose(Axis.ZP.rotationDegrees(-70.0F));
        MythicMesh.beam(vc, ps.last(), 0.08F, 1.05F, 0.79F,0.42F,1.0F,0.95F);
        ps.translate(0.0F, 0.83F, 0.0F);
        ps.mulPose(Axis.ZP.rotationDegrees(-28.0F));
        MythicMesh.beam(vc, ps.last(), 0.055F, 0.68F, 0.96F,0.75F,1.0F,0.85F);
        ps.popPose();
    }

    private static float[] color(int variant) {
        return switch (variant) {
            case 0 -> new float[]{0.62F,0.88F,1.0F};
            case 1 -> new float[]{0.45F,0.80F,1.0F};
            case 2 -> new float[]{0.20F,0.94F,1.0F};
            case 3 -> new float[]{1.0F,0.68F,0.12F};
            case 4 -> new float[]{0.66F,0.30F,1.0F};
            default -> new float[]{1.0F,1.0F,1.0F};
        };
    }

    @Override
    public ResourceLocation getTextureLocation(DivineProjectileEntity entity) {
        return TEXTURE;
    }
}
