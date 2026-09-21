package igy.iradeseus;

import igy.iradeseus.entity.DivineProjectileEntity;
import igy.iradeseus.entity.MythicAvatarEntity;
import igy.iradeseus.entity.RitualFxEntity;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(IraDeSeus3D.MOD_ID)
public final class IraDeSeus3D {
    public static final String MOD_ID = "iradeseus3d";

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);

    public static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLES =
        DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MOD_ID);

    public static final RegistryObject<EntityType<MythicAvatarEntity>> MYTHIC_AVATAR =
        ENTITY_TYPES.register("mythic_avatar", () ->
            EntityType.Builder.<MythicAvatarEntity>of(MythicAvatarEntity::new, MobCategory.MISC)
                .sized(1.35F, 3.5F)
                .clientTrackingRange(160)
                .updateInterval(1)
                .build(new ResourceLocation(MOD_ID, "mythic_avatar").toString()));

    public static final RegistryObject<EntityType<RitualFxEntity>> RITUAL_FX =
        ENTITY_TYPES.register("ritual_fx", () ->
            EntityType.Builder.<RitualFxEntity>of(RitualFxEntity::new, MobCategory.MISC)
                .sized(0.2F, 0.2F)
                .clientTrackingRange(192)
                .updateInterval(1)
                .build(new ResourceLocation(MOD_ID, "ritual_fx").toString()));

    public static final RegistryObject<EntityType<DivineProjectileEntity>> DIVINE_PROJECTILE =
        ENTITY_TYPES.register("divine_projectile", () ->
            EntityType.Builder.<DivineProjectileEntity>of(DivineProjectileEntity::new, MobCategory.MISC)
                .sized(0.45F, 1.6F)
                .clientTrackingRange(192)
                .updateInterval(1)
                .build(new ResourceLocation(MOD_ID, "divine_projectile").toString()));

    public static final RegistryObject<SimpleParticleType> DIVINE_MOTE =
        PARTICLES.register("divine_mote", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> ARC_SPARK =
        PARTICLES.register("arc_spark", () -> new SimpleParticleType(false));

    public IraDeSeus3D() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITY_TYPES.register(modBus);
        PARTICLES.register(modBus);
    }
}
