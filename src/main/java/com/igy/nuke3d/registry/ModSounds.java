package com.igy.nuke3d.registry;

import com.igy.nuke3d.Nuke3D;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Nuke3D.MOD_ID);

    public static final RegistryObject<SoundEvent> NUKE_SEQUENCE = SOUND_EVENTS.register(
            "nuke_sequence",
            () -> SoundEvent.createVariableRangeEvent(
                    new ResourceLocation(Nuke3D.MOD_ID, "nuke_sequence")
            )
    );

    private ModSounds() {}
}
