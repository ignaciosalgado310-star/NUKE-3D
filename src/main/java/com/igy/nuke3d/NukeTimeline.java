package com.igy.nuke3d;

/**
 * Shared cinematic timing for the faster NUKE sequence.
 *
 * The edited soundtrack keeps the opening and the explosion section only. The bomb now reaches
 * the ground at 4.5 seconds (90 ticks) instead of taking almost 12 seconds to descend.
 */
public final class NukeTimeline {
    public static final int SEQUENCE_TICKS = 230;
    public static final int IMPACT_TICK = 90;

    /** Existing bomb renderers use a 43% impact ratio. Feed them this synthetic duration so the
     * unchanged bomb model reaches the ground exactly at IMPACT_TICK. */
    public static final int LEGACY_BOMB_DURATION = Math.round(IMPACT_TICK / 0.43f);

    private NukeTimeline() {}
}
