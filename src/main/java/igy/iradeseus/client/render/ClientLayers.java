package igy.iradeseus.client.render;

import igy.iradeseus.IraDeSeus3D;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public final class ClientLayers {
    public static final ModelLayerLocation MYTHIC_AVATAR =
        new ModelLayerLocation(new ResourceLocation(IraDeSeus3D.MOD_ID, "mythic_avatar"), "main");

    private ClientLayers() {}
}
