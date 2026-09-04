# NUKE 3D

Standalone Forge 1.20.1 version of the **NUKE** attack.

This build focuses on a cinematic 3D nuclear strike with no Minecraft particle effects: the bomb, flames, shock rings, debris and mushroom cloud are rendered with procedural meshes and textures.

## Command

The NUKE can now target **any online player**:

```text
/destruction nuke player <jugador> <totems>
```

Examples:

```text
/destruction nuke player Steve 25
/destruction nuke player Alex 300
```

Optional custom damage value:

```text
/destruction nuke player <jugador> <totems> <damage_hearts>
```

## Version 1.1.0 changes

- Removed the old hard lock to a single Minecraft username.
- Reworked the falling bomb into a more detailed layered 3D model with body bands, tail fins, nozzle and multi-layer engine flame.
- Expanded the explosion with a brighter impact flash, denser rolling mushroom cloud, hot underside, shock rings and more visible 3D debris.
- Reworked terrain destruction into a rounded lower hemisphere. With the default setup the crater profile forms a clean U / half-circle instead of an irregular shallow bowl.
- Bedrock is still preserved.
- PURPURE-style targeted totem consumption and vanilla totem activation animation are retained.

## Compatibility

- Minecraft Java 1.20.1
- Forge 47.3.5
- Java 17
- Mohist-compatible Forge-side implementation
- PURPURE-style exact totem consumption: one requested totem every 2 ticks
- Vanilla totem activation animation retained
- No Minecraft particle effects; procedural 3D meshes/textures
