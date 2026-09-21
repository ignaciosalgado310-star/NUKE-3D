package igy.iradeseus.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import igy.iradeseus.entity.MythicAvatarEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public final class MythicAvatarModel extends EntityModel<MythicAvatarEntity> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart crown;
    private final ModelPart torso;
    private final ModelPart chestPlate;
    private final ModelPart cape;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftForearm;
    private final ModelPart rightForearm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart leftGreave;
    private final ModelPart rightGreave;

    private final ModelPart zeusBolt;
    private final ModelPart thorHammer;
    private final ModelPart poseidonTrident;
    private final ModelPart raStaff;
    private final ModelPart hadesScythe;

    public MythicAvatarModel(ModelPart bakedRoot) {
        this.root = bakedRoot.getChild("root");
        this.head = root.getChild("head");
        this.crown = head.getChild("crown");
        this.torso = root.getChild("torso");
        this.chestPlate = torso.getChild("chest_plate");
        this.cape = torso.getChild("cape");
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.leftForearm = leftArm.getChild("left_forearm");
        this.rightForearm = rightArm.getChild("right_forearm");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.leftGreave = leftLeg.getChild("left_greave");
        this.rightGreave = rightLeg.getChild("right_greave");

        this.zeusBolt = rightForearm.getChild("zeus_bolt");
        this.thorHammer = rightForearm.getChild("thor_hammer");
        this.poseidonTrident = rightForearm.getChild("poseidon_trident");
        this.raStaff = rightForearm.getChild("ra_staff");
        this.hadesScythe = rightForearm.getChild("hades_scythe");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition meshRoot = mesh.getRoot();

        PartDefinition root = meshRoot.addOrReplaceChild("root", CubeListBuilder.create(),
            PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition torso = root.addOrReplaceChild("torso",
            CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.5F, -10.0F, -2.4F, 9.0F, 10.0F, 4.8F,
                    new CubeDeformation(0.15F))
                .texOffs(0, 15).addBox(-5.2F, -9.2F, -2.8F, 10.4F, 2.1F, 5.6F,
                    new CubeDeformation(0.35F)),
            PartPose.offset(0.0F, -12.0F, 0.0F));

        torso.addOrReplaceChild("chest_plate",
            CubeListBuilder.create()
                .texOffs(38, 0).addBox(-4.7F, -9.3F, -3.0F, 9.4F, 7.2F, 1.2F,
                    new CubeDeformation(0.2F))
                .texOffs(38, 10).addBox(-3.0F, -8.8F, -3.7F, 6.0F, 4.0F, 0.9F,
                    new CubeDeformation(0.1F))
                .texOffs(39, 16).addBox(-1.2F, -8.4F, -4.1F, 2.4F, 2.4F, 0.7F,
                    new CubeDeformation(0.05F)),
            PartPose.ZERO);

        torso.addOrReplaceChild("cape",
            CubeListBuilder.create()
                .texOffs(0, 72).addBox(-4.3F, -8.7F, 2.4F, 8.6F, 11.5F, 0.7F,
                    new CubeDeformation(0.05F))
                .texOffs(20, 72).addBox(-3.4F, 2.0F, 2.5F, 6.8F, 5.5F, 0.5F,
                    new CubeDeformation(0.0F)),
            PartPose.rotation(0.08F, 0.0F, 0.0F));

        PartDefinition head = root.addOrReplaceChild("head",
            CubeListBuilder.create()
                .texOffs(0, 24).addBox(-3.6F, -7.2F, -3.5F, 7.2F, 7.2F, 7.0F,
                    new CubeDeformation(0.0F))
                .texOffs(30, 24).addBox(-3.8F, -7.5F, 1.8F, 7.6F, 7.7F, 2.1F,
                    new CubeDeformation(0.15F))
                .texOffs(50, 24).addBox(-3.5F, -1.0F, -3.9F, 7.0F, 3.0F, 1.0F,
                    new CubeDeformation(0.1F)),
            PartPose.offset(0.0F, -22.0F, 0.0F));

        head.addOrReplaceChild("crown",
            CubeListBuilder.create()
                .texOffs(70, 0).addBox(-4.0F, -9.2F, -3.7F, 8.0F, 2.0F, 7.4F,
                    new CubeDeformation(0.05F))
                .texOffs(70, 10).addBox(-3.4F, -12.0F, -2.9F, 1.2F, 3.0F, 1.4F,
                    new CubeDeformation(0.05F))
                .texOffs(76, 10).addBox(-0.6F, -13.0F, -3.0F, 1.2F, 4.0F, 1.5F,
                    new CubeDeformation(0.05F))
                .texOffs(82, 10).addBox(2.2F, -12.0F, -2.9F, 1.2F, 3.0F, 1.4F,
                    new CubeDeformation(0.05F)),
            PartPose.ZERO);

        PartDefinition leftArm = root.addOrReplaceChild("left_arm",
            CubeListBuilder.create()
                .texOffs(0, 42).addBox(-3.7F, -2.7F, -2.7F, 4.0F, 7.0F, 5.4F,
                    new CubeDeformation(0.2F))
                .texOffs(20, 42).addBox(-4.4F, -3.7F, -3.3F, 4.8F, 2.5F, 6.6F,
                    new CubeDeformation(0.35F)),
            PartPose.offset(5.0F, -20.0F, 0.0F));

        PartDefinition rightArm = root.addOrReplaceChild("right_arm",
            CubeListBuilder.create()
                .texOffs(0, 42).mirror().addBox(-0.3F, -2.7F, -2.7F, 4.0F, 7.0F, 5.4F,
                    new CubeDeformation(0.2F)).mirror(false)
                .texOffs(20, 42).mirror().addBox(-0.4F, -3.7F, -3.3F, 4.8F, 2.5F, 6.6F,
                    new CubeDeformation(0.35F)).mirror(false),
            PartPose.offset(-5.0F, -20.0F, 0.0F));

        PartDefinition leftForearm = leftArm.addOrReplaceChild("left_forearm",
            CubeListBuilder.create()
                .texOffs(42, 40).addBox(-2.0F, 0.0F, -2.1F, 4.0F, 7.0F, 4.2F,
                    new CubeDeformation(0.05F))
                .texOffs(60, 40).addBox(-2.35F, 2.0F, -2.45F, 4.7F, 2.6F, 4.9F,
                    new CubeDeformation(0.15F)),
            PartPose.offset(-1.7F, 4.0F, 0.0F));

        PartDefinition rightForearm = rightArm.addOrReplaceChild("right_forearm",
            CubeListBuilder.create()
                .texOffs(42, 40).mirror().addBox(-2.0F, 0.0F, -2.1F, 4.0F, 7.0F, 4.2F,
                    new CubeDeformation(0.05F)).mirror(false)
                .texOffs(60, 40).mirror().addBox(-2.35F, 2.0F, -2.45F, 4.7F, 2.6F, 4.9F,
                    new CubeDeformation(0.15F)).mirror(false),
            PartPose.offset(1.7F, 4.0F, 0.0F));

        PartDefinition leftLeg = root.addOrReplaceChild("left_leg",
            CubeListBuilder.create()
                .texOffs(0, 56).addBox(-2.7F, 0.0F, -2.7F, 5.0F, 8.0F, 5.2F,
                    new CubeDeformation(0.15F)),
            PartPose.offset(2.4F, -12.0F, 0.0F));

        PartDefinition rightLeg = root.addOrReplaceChild("right_leg",
            CubeListBuilder.create()
                .texOffs(0, 56).mirror().addBox(-2.3F, 0.0F, -2.7F, 5.0F, 8.0F, 5.2F,
                    new CubeDeformation(0.15F)).mirror(false),
            PartPose.offset(-2.4F, -12.0F, 0.0F));

        leftLeg.addOrReplaceChild("left_greave",
            CubeListBuilder.create()
                .texOffs(24, 55).addBox(-2.45F, 0.0F, -2.65F, 4.8F, 7.0F, 5.0F,
                    new CubeDeformation(0.2F))
                .texOffs(44, 55).addBox(-2.6F, 5.3F, -3.2F, 5.2F, 2.0F, 6.0F,
                    new CubeDeformation(0.18F)),
            PartPose.offset(-0.2F, 7.0F, 0.0F));

        rightLeg.addOrReplaceChild("right_greave",
            CubeListBuilder.create()
                .texOffs(24, 55).mirror().addBox(-2.35F, 0.0F, -2.65F, 4.8F, 7.0F, 5.0F,
                    new CubeDeformation(0.2F)).mirror(false)
                .texOffs(44, 55).mirror().addBox(-2.6F, 5.3F, -3.2F, 5.2F, 2.0F, 6.0F,
                    new CubeDeformation(0.18F)).mirror(false),
            PartPose.offset(0.2F, 7.0F, 0.0F));

        rightForearm.addOrReplaceChild("zeus_bolt",
            CubeListBuilder.create()
                .texOffs(70, 26).addBox(-0.7F, 5.5F, -0.7F, 1.4F, 12.0F, 1.4F,
                    new CubeDeformation(0.05F))
                .texOffs(76, 26).addBox(-1.8F, 8.0F, -1.0F, 3.6F, 3.0F, 2.0F,
                    new CubeDeformation(0.0F))
                .texOffs(88, 26).addBox(-1.5F, 14.0F, -0.9F, 3.0F, 2.4F, 1.8F,
                    new CubeDeformation(0.0F)),
            PartPose.rotation(0.15F, 0.0F, -0.18F));

        rightForearm.addOrReplaceChild("thor_hammer",
            CubeListBuilder.create()
                .texOffs(70, 44).addBox(-0.8F, 5.0F, -0.8F, 1.6F, 10.0F, 1.6F,
                    new CubeDeformation(0.0F))
                .texOffs(78, 42).addBox(-4.2F, 12.0F, -2.5F, 8.4F, 5.0F, 5.0F,
                    new CubeDeformation(0.3F))
                .texOffs(78, 53).addBox(-3.6F, 11.2F, -1.9F, 7.2F, 1.0F, 3.8F,
                    new CubeDeformation(0.1F)),
            PartPose.rotation(0.1F, 0.0F, -0.2F));

        rightForearm.addOrReplaceChild("poseidon_trident",
            CubeListBuilder.create()
                .texOffs(104, 0).addBox(-0.6F, 4.0F, -0.6F, 1.2F, 18.0F, 1.2F,
                    new CubeDeformation(0.05F))
                .texOffs(110, 0).addBox(-4.2F, 20.0F, -0.55F, 8.4F, 1.1F, 1.1F,
                    new CubeDeformation(0.0F))
                .texOffs(110, 4).addBox(-4.1F, 17.5F, -0.5F, 1.0F, 5.0F, 1.0F,
                    new CubeDeformation(0.0F))
                .texOffs(116, 4).addBox(-0.5F, 17.0F, -0.5F, 1.0F, 6.0F, 1.0F,
                    new CubeDeformation(0.0F))
                .texOffs(122, 4).addBox(3.1F, 17.5F, -0.5F, 1.0F, 5.0F, 1.0F,
                    new CubeDeformation(0.0F)),
            PartPose.rotation(0.12F, 0.0F, -0.14F));

        rightForearm.addOrReplaceChild("ra_staff",
            CubeListBuilder.create()
                .texOffs(104, 24).addBox(-0.65F, 4.0F, -0.65F, 1.3F, 18.0F, 1.3F,
                    new CubeDeformation(0.03F))
                .texOffs(112, 24).addBox(-3.0F, 18.5F, -1.0F, 6.0F, 6.0F, 2.0F,
                    new CubeDeformation(0.2F))
                .texOffs(114, 33).addBox(-1.4F, 17.1F, -1.4F, 2.8F, 8.8F, 2.8F,
                    new CubeDeformation(-0.4F)),
            PartPose.rotation(0.08F, 0.0F, -0.15F));

        rightForearm.addOrReplaceChild("hades_scythe",
            CubeListBuilder.create()
                .texOffs(100, 48).addBox(-0.65F, 3.0F, -0.65F, 1.3F, 20.0F, 1.3F,
                    new CubeDeformation(0.02F))
                .texOffs(108, 49).addBox(0.0F, 20.0F, -0.7F, 7.0F, 1.4F, 1.4F,
                    new CubeDeformation(0.0F))
                .texOffs(109, 54).addBox(5.0F, 18.2F, -0.6F, 4.8F, 1.2F, 1.2F,
                    new CubeDeformation(0.0F))
                .texOffs(111, 59).addBox(8.2F, 15.7F, -0.5F, 3.6F, 1.0F, 1.0F,
                    new CubeDeformation(0.0F)),
            PartPose.rotation(0.15F, 0.0F, -0.28F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(MythicAvatarEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        int variant = entity.getVariant();
        float t = ageInTicks * 0.085F;
        float breathe = Mth.sin(t) * 0.045F;
        float power = entity.getPower();

        head.yRot = netHeadYaw * ((float) Math.PI / 180.0F) * 0.28F;
        head.xRot = headPitch * ((float) Math.PI / 180.0F) * 0.18F + breathe;
        crown.yRot = Mth.sin(t * 0.55F) * 0.035F;

        torso.xRot = -0.025F + breathe * 0.35F;
        chestPlate.zRot = Mth.sin(t * 0.7F) * 0.018F;
        cape.xRot = 0.10F + Mth.sin(t * 0.72F) * 0.07F * power;

        leftArm.xRot = -0.15F + Mth.sin(t * 0.72F + 1.4F) * 0.16F;
        leftArm.zRot = -0.13F + Mth.sin(t * 0.45F) * 0.08F;
        rightArm.xRot = -0.48F + Mth.sin(t * 0.75F) * 0.13F;
        rightArm.zRot = 0.19F + Mth.sin(t * 0.52F) * 0.07F;
        leftForearm.xRot = -0.16F + Mth.sin(t * 0.66F) * 0.09F;
        rightForearm.xRot = -0.38F + Mth.sin(t * 0.8F) * 0.11F;

        leftLeg.xRot = Mth.sin(t * 0.38F) * 0.035F;
        rightLeg.xRot = -leftLeg.xRot;
        leftGreave.xRot = Mth.sin(t * 0.4F + 1.2F) * 0.018F;
        rightGreave.xRot = -leftGreave.xRot;

        zeusBolt.visible = variant == 0;
        thorHammer.visible = variant == 1;
        poseidonTrident.visible = variant == 2;
        raStaff.visible = variant == 3;
        hadesScythe.visible = variant == 4;

        if (variant == 0) {
            rightArm.xRot = -1.18F + Mth.sin(t * 1.15F) * 0.12F;
            rightForearm.xRot = -0.48F;
        } else if (variant == 1) {
            rightArm.xRot = -1.0F + Mth.sin(t * 0.92F) * 0.10F;
            rightArm.zRot = 0.42F;
        } else if (variant == 2) {
            rightArm.xRot = -0.72F;
            rightForearm.xRot = -0.12F + Mth.sin(t) * 0.05F;
        } else if (variant == 3) {
            rightArm.xRot = -0.9F;
            leftArm.xRot = -0.62F + Mth.sin(t * 0.65F) * 0.08F;
        } else if (variant == 4) {
            rightArm.xRot = -0.82F;
            rightArm.zRot = 0.55F;
            head.yRot += Mth.sin(t * 0.3F) * 0.10F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
