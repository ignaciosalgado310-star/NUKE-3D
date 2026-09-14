package com.igy.nuke3d.sound;

import com.igy.nuke3d.Nuke3D;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Nuke3D.MOD_ID);

    public static final RegistryObject<SoundEvent> NUKE_SEQUENCE = SOUNDS.register(
            "nuke_sequence",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Nuke3D.MOD_ID, "nuke_sequence"))
    );

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }

    private ModSounds() {}
}
