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

public final class RitualFxEntity extends Entity {
    private static final EntityDataAccessor<Integer> VARIANT =
        SynchedEntityData.defineId(RitualFxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> COLOR =
        SynchedEntityData.defineId(RitualFxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> FX_SCALE =
        SynchedEntityData.defineId(RitualFxEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> LIFETIME =
        SynchedEntityData.defineId(RitualFxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SPIN =
        SynchedEntityData.defineId(RitualFxEntity.class, EntityDataSerializers.FLOAT);

    public RitualFxEntity(EntityType<? extends RitualFxEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setInvulnerable(true);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(VARIANT, 0);
        this.entityData.define(COLOR, 0xA8D8FF);
        this.entityData.define(FX_SCALE, 1.0F);
        this.entityData.define(LIFETIME, 120);
        this.entityData.define(SPIN, 1.0F);
    }

    public int getVariant() { return this.entityData.get(VARIANT); }
    public int getColor() { return this.entityData.get(COLOR); }
    public float getFxScale() { return this.entityData.get(FX_SCALE); }
    public int getLifetime() { return this.entityData.get(LIFETIME); }
    public float getSpin() { return this.entityData.get(SPIN); }

    public void configure(int variant, int color, float scale, int lifetime, float spin) {
        this.entityData.set(VARIANT, Math.max(0, variant));
        this.entityData.set(COLOR, color & 0xFFFFFF);
        this.entityData.set(FX_SCALE, Math.max(0.05F, scale));
        this.entityData.set(LIFETIME, Math.max(1, lifetime));
        this.entityData.set(SPIN, spin);
    }

    @Override
    public void tick() {
        super.tick();
        this.fallDistance = 0.0F;
        if (!this.level().isClientSide && this.tickCount > getLifetime()) {
            this.discard();
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        configure(tag.getInt("Variant"), tag.getInt("Color"), tag.getFloat("FxScale"),
            tag.getInt("Lifetime"), tag.getFloat("Spin"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Variant", getVariant());
        tag.putInt("Color", getColor());
        tag.putFloat("FxScale", getFxScale());
        tag.putInt("Lifetime", getLifetime());
        tag.putFloat("Spin", getSpin());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
