package com.igy.nuke3d.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class NukeConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue COMMAND_PERMISSION_LEVEL;
    public static final ForgeConfigSpec.IntValue MAX_ACTIVE_NUKES;
    public static final ForgeConfigSpec.BooleanValue BROADCAST_START;
    public static final ForgeConfigSpec.BooleanValue ALLOW_TERRAIN_DAMAGE;
    public static final ForgeConfigSpec.IntValue MAX_BLOCK_CHANGES_PER_TICK;

    public static final ForgeConfigSpec.DoubleValue DAMAGE_HEARTS;
    public static final ForgeConfigSpec.IntValue DAMAGE_RADIUS;
    public static final ForgeConfigSpec.IntValue DURATION_TICKS;
    public static final ForgeConfigSpec.IntValue TERRAIN_RADIUS;
    public static final ForgeConfigSpec.IntValue DAMAGE_PULSES;
    public static final ForgeConfigSpec.IntValue PULSE_INTERVAL_TICKS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("general");
        COMMAND_PERMISSION_LEVEL = builder.comment("Permission level required for /destruction nuke.")
                .defineInRange("commandPermissionLevel", 2, 0, 4);
        MAX_ACTIVE_NUKES = builder.comment("Maximum simultaneous NUKE events.")
                .defineInRange("maxActiveNukes", 12, 1, 100);
        BROADCAST_START = builder.comment("Broadcast a message when a NUKE starts.")
                .define("broadcastStart", true);
        ALLOW_TERRAIN_DAMAGE = builder.comment("Master switch for block destruction.")
                .define("allowTerrainDamage", true);
        MAX_BLOCK_CHANGES_PER_TICK = builder.comment("Safety budget for block changes per NUKE tick.")
                .defineInRange("maxBlockChangesPerTick", 700, 0, 10000);
        builder.pop();

        builder.push("nuke");
        DAMAGE_HEARTS = builder.comment("Damage per pulse in hearts. Kept identical to DESASTRE-3D NUKE.")
                .defineInRange("damageHearts", 20.0, 0.0, 50000.0);
        DAMAGE_RADIUS = builder.comment("Entity damage radius in blocks.")
                .defineInRange("damageRadius", 35, 1, 256);
        DURATION_TICKS = builder.comment("Base duration in ticks. 20 ticks = 1 second.")
                .defineInRange("durationTicks", 140, 20, 12000);
        TERRAIN_RADIUS = builder.comment("Base terrain radius. The NUKE uses the same x3/min-48 rule as DESASTRE-3D.")
                .defineInRange("terrainRadius", 18, 0, 96);
        DAMAGE_PULSES = builder.comment("Default requested totems if no count is supplied.")
                .defineInRange("damagePulses", 2, 0, 1000);
        PULSE_INTERVAL_TICKS = builder.comment("Non-target pulse interval; targeted mode uses PURPURE's exact 2-tick cadence.")
                .defineInRange("pulseIntervalTicks", 8, 1, 1200);
        builder.pop();

        SPEC = builder.build();
    }

    private NukeConfig() {}
}
