# NUKE 3D — Nelonino Edition

Standalone Forge 1.20.1 version of the **NUKE** from `DESASTRE-3D` 3.2.1.

Only the NUKE attack is included. Laser, planet, supernova and black hole are not present.

## Locked target

The attack is hard-locked server-side to the Minecraft username `Nelonino`.

```text
/destruction nuke player Nelonino <totems>
```

Examples:

```text
/destruction nuke player Nelonino 25
/destruction nuke player Nelonino 300
```

Any other target is rejected before the event starts. There is no coordinate/here attack command, so the target restriction cannot be bypassed through this mod's command tree.

## Compatibility

- Minecraft Java 1.20.1
- Forge 47.3.5
- Java 17
- Mohist-compatible Forge-side implementation
- PURPURE-style exact totem consumption: one requested totem every 2 ticks
- Vanilla totem activation animation retained
- No Minecraft particle effects; procedural 3D meshes/textures
