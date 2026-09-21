package igy.iradeseus.entity;

import igy.iradeseus.IraDeSeus3D;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public final class DivineProjectileEntity extends Entity {
    private static final EntityDataAccessor<Integer> VARIANT =
        SynchedEntityData.defineId(DivineProjectileEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> MODEL_SCALE =
        SynchedEntityData.defineId(DivineProjectileEntity.class, EntityDataSerializers.FLOAT);

    private double targetX;
    private double targetY;
    private double targetZ;
    private boolean hasTarget;

    public DivineProjectileEntity(EntityType<? extends DivineProjectileEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(VARIANT, 0);
        this.entityData.define(MODEL_SCALE, 1.0F);
    }

    public int getVariant() { return this.entityData.get(VARIANT); }
    public float getModelScale() { return this.entityData.get(MODEL_SCALE); }

    public void configure(int variant, float scale, Vec3 target) {
        this.entityData.set(VARIANT, Math.max(0, Math.min(4, variant)));
        this.entityData.set(MODEL_SCALE, Math.max(0.2F, scale));
        this.targetX = target.x;
        this.targetY = target.y;
        this.targetZ = target.z;
        this.hasTarget = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }

        if (!hasTarget) {
            if (this.tickCount > 80) this.discard();
            return;
        }

        Vec3 target = new Vec3(targetX, targetY, targetZ);
        Vec3 delta = target.subtract(this.position());
        double distance = delta.length();

        if (distance < 1.35 || this.tickCount > 90) {
            impact((ServerLevel) this.level());
            this.discard();
            return;
        }

        double speed = 0.95 + Math.min(1.15, this.tickCount * 0.018);
        Vec3 motion = delta.normalize().scale(speed);
        this.setDeltaMovement(motion);
        this.move(MoverType.SELF, motion);
        this.hurtMarked = true;

        ServerLevel level = (ServerLevel) this.level();
        if (this.tickCount % 2 == 0) {
            int colorCount = getVariant() == 3 ? 12 : 7;
            level.sendParticles(IraDeSeus3D.ARC_SPARK.get(),
                this.getX(), this.getY(), this.getZ(),
                colorCount, 0.22, 0.22, 0.22, 0.02);
        }
    }

    private void impact(ServerLevel level) {
        level.playSound(null, this.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER,
            SoundSource.MASTER, 2.6F, 0.62F + getVariant() * 0.07F);
        level.sendParticles(IraDeSeus3D.DIVINE_MOTE.get(),
            targetX, targetY + 1.0, targetZ,
            55, 2.8, 2.2, 2.8, 0.12);
        level.sendParticles(IraDeSeus3D.ARC_SPARK.get(),
            targetX, targetY + 1.0, targetZ,
            75, 3.5, 2.8, 3.5, 0.18);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(VARIANT, tag.getInt("Variant"));
        this.entityData.set(MODEL_SCALE, tag.getFloat("ModelScale"));
        this.targetX = tag.getDouble("TargetX");
        this.targetY = tag.getDouble("TargetY");
        this.targetZ = tag.getDouble("TargetZ");
        this.hasTarget = tag.getBoolean("HasTarget");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Variant", getVariant());
        tag.putFloat("ModelScale", getModelScale());
        tag.putDouble("TargetX", targetX);
        tag.putDouble("TargetY", targetY);
        tag.putDouble("TargetZ", targetZ);
        tag.putBoolean("HasTarget", hasTarget);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
