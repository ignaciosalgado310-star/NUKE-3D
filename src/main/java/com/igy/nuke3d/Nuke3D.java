package com.igy.nuke3d;

import com.igy.nuke3d.config.NukeConfig;
import com.igy.nuke3d.network.ModNetwork;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(Nuke3D.MOD_ID)
public final class Nuke3D {
    public static final String MOD_ID = "nuke3d";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Nuke3D() {
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.COMMON,
                NukeConfig.SPEC,
                "nuke3d-common.toml"
        );
        ModNetwork.register();
    }
}
