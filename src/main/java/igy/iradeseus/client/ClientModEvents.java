package igy.iradeseus.client;

import igy.iradeseus.IraDeSeus3D;
import igy.iradeseus.client.model.MythicAvatarModel;
import igy.iradeseus.client.particle.ArcSparkParticle;
import igy.iradeseus.client.particle.DivineMoteParticle;
import igy.iradeseus.client.render.ClientLayers;
import igy.iradeseus.client.render.DivineProjectileRenderer;
import igy.iradeseus.client.render.MythicAvatarRenderer;
import igy.iradeseus.client.render.RitualFxRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = IraDeSeus3D.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(IraDeSeus3D.MYTHIC_AVATAR.get(), MythicAvatarRenderer::new);
        event.registerEntityRenderer(IraDeSeus3D.RITUAL_FX.get(), RitualFxRenderer::new);
        event.registerEntityRenderer(IraDeSeus3D.DIVINE_PROJECTILE.get(), DivineProjectileRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ClientLayers.MYTHIC_AVATAR, MythicAvatarModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(IraDeSeus3D.DIVINE_MOTE.get(), DivineMoteParticle.Provider::new);
        event.registerSpriteSet(IraDeSeus3D.ARC_SPARK.get(), ArcSparkParticle.Provider::new);
    }
}
