package com.igy.nuke3d;

/**
 * Shared cinematic timing for the NUKE sequence.
 *
 * The replacement audio supplied for NUKE-3D is 17.92 seconds long. At 20 ticks/second that is
 * about 358.4 ticks, so the visual sequence is intentionally fixed at 359 ticks. The strongest
 * impact in the supplied audio lands at roughly 11.8 seconds, which maps to tick 236.
 */
public final class NukeTimeline {
    public static final int SEQUENCE_TICKS = 359;
    public static final int IMPACT_TICK = 236;

    /** Existing bomb renderers use a 43% impact ratio. Feed them this synthetic duration so the
     * unchanged bomb model reaches the ground exactly at IMPACT_TICK. */
    public static final int LEGACY_BOMB_DURATION = Math.round(IMPACT_TICK / 0.43f);

    private NukeTimeline() {}
}
