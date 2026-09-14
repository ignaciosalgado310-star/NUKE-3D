package com.igy.nuke3d;

/**
 * Shared cinematic timing for the custom NUKE audio.
 *
 * The supplied sound's main detonation begins at roughly 11.25 seconds,
 * therefore the real terrain impact and every client-side impact effect
 * are locked to tick 225 (20 ticks = 1 second).
 */
public final class NukeTimeline {
    public static final int IMPACT_TICK = 225;
    public static final int AUDIO_DURATION_TICKS = 359;
    public static final int MISSILE_ENTRY_TICK = 62;

    private NukeTimeline() {}

    public static int visualDuration(int configuredDuration) {
        return Math.max(AUDIO_DURATION_TICKS, configuredDuration);
    }
}
