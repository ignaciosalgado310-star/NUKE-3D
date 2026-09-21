package igy.iradeseus.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public final class MythicAvatarEntity extends Entity {
    private static final EntityDataAccessor<Integer> VARIANT =
        SynchedEntityData.defineId(MythicAvatarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> POWER =
        SynchedEntityData.defineId(MythicAvatarEntity.class, EntityDataSerializers.FLOAT);

    public MythicAvatarEntity(EntityType<? extends MythicAvatarEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(VARIANT, 0);
        this.entityData.define(POWER, 1.0F);
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, Math.max(0, Math.min(4, variant)));
    }

    public float getPower() {
        return this.entityData.get(POWER);
    }

    public void setPower(float power) {
        this.entityData.set(POWER, Math.max(0.1F, power));
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(0.0, 0.0, 0.0);
        this.fallDistance = 0.0F;
        if (!this.level().isClientSide && this.tickCount > 1600) {
            this.discard();
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setVariant(tag.getInt("Variant"));
        setPower(tag.contains("Power") ? tag.getFloat("Power") : 1.0F);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Variant", getVariant());
        tag.putFloat("Power", getPower());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
