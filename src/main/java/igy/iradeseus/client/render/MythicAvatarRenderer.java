package igy.iradeseus.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import igy.iradeseus.IraDeSeus3D;
import igy.iradeseus.client.model.MythicAvatarModel;
import igy.iradeseus.entity.MythicAvatarEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class MythicAvatarRenderer extends EntityRenderer<MythicAvatarEntity> {
    public static final ResourceLocation TEXTURE =
        new ResourceLocation(IraDeSeus3D.MOD_ID, "textures/entity/mythic_avatar.png");

    private final MythicAvatarModel model;

    public MythicAvatarRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new MythicAvatarModel(context.bakeLayer(ClientLayers.MYTHIC_AVATAR));
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(MythicAvatarEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float age = entity.tickCount + partialTicks;
        float bob = Mth.sin(age * 0.055F + entity.getVariant()) * 0.12F;
        float power = entity.getPower();
        float scale = (1.72F + entity.getVariant() * 0.025F) * (0.96F + power * 0.04F);

        poseStack.translate(0.0F, 3.15F + bob, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
        poseStack.scale(-scale, -scale, scale);
        poseStack.translate(0.0F, -1.5F, 0.0F);

        model.setupAnim(entity, 0.0F, 0.0F, age, 0.0F, 0.0F);

        float[] tint = tint(entity.getVariant());
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        model.renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT,
            OverlayTexture.NO_OVERLAY, tint[0], tint[1], tint[2], 0.98F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static float[] tint(int variant) {
        return switch (variant) {
            case 0 -> new float[]{0.78F, 0.91F, 1.0F};
            case 1 -> new float[]{0.70F, 0.86F, 1.0F};
            case 2 -> new float[]{0.45F, 0.96F, 1.0F};
            case 3 -> new float[]{1.0F, 0.80F, 0.34F};
            case 4 -> new float[]{0.77F, 0.48F, 1.0F};
            default -> new float[]{1.0F, 1.0F, 1.0F};
        };
    }

    @Override
    public ResourceLocation getTextureLocation(MythicAvatarEntity entity) {
        return TEXTURE;
    }
}
