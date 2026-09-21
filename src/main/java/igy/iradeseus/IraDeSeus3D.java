package igy.iradeseus;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

@Mod(IraDeSeus3D.MOD_ID)
@Mod.EventBusSubscriber(modid = IraDeSeus3D.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class IraDeSeus3D {
    public static final String MOD_ID = "iradeseus3d";
    private static final Map<UUID, RitualState> ACTIVE = new HashMap<>();

    public IraDeSeus3D() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event, "iradeseus");
        register(event, "iraseus");
        register(event, "zeusira");
    }

    private static void register(RegisterCommandsEvent event, String root) {
        event.getDispatcher().register(
            Commands.literal(root)
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("jugador", EntityArgument.player())
                    .then(Commands.argument("totems", IntegerArgumentType.integer(1, 9999))
                        .executes(ctx -> {
                            ServerPlayer player = EntityArgument.getPlayer(ctx, "jugador");
                            int totems = IntegerArgumentType.getInteger(ctx, "totems");
                            start(player, totems);
                            ctx.getSource().sendSuccess(() ->
                                Component.literal("§6[IRA-DE-SEUS-3D] §fRitual iniciado sobre §e" +
                                    player.getGameProfile().getName() + " §fcon §b" + totems + " §ftótems."), true);
                            return 1;
                        })
                    )
                )
        );
    }

    private static void start(ServerPlayer player, int totems) {
        RitualState old = ACTIVE.remove(player.getUUID());
        if (old != null) {
            old.cleanup();
        }
        RitualState state = new RitualState(player, totems);
        ACTIVE.put(player.getUUID(), state);
        state.begin();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ACTIVE.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, RitualState>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            RitualState state = iterator.next().getValue();
            if (state.tick()) {
                state.cleanup();
                iterator.remove();
            }
        }
    }

    private static final class RitualState {
        private final ServerPlayer player;
        private final ServerLevel level;
        private final BlockPos center;
        private final int requestedTotems;
        private final List<UUID> displays = new ArrayList<>();
        private final List<UUID> orbiters = new ArrayList<>();
        private final List<Relic> relics = new ArrayList<>();
        private final Queue<BlockPos> craterQueue = new ArrayDeque<>();

        private int tick;
        private int totemsTriggered;
        private boolean craterPrepared;
        private boolean craterDecorated;

        RitualState(ServerPlayer player, int requestedTotems) {
            this.player = player;
            this.level = (ServerLevel) player.level();
            this.center = player.blockPosition();
            this.requestedTotems = requestedTotems;
        }

        void begin() {
            player.sendSystemMessage(Component.literal("§5✦ §dEL CIELO RESPONDE... §5✦"));
            player.sendSystemMessage(Component.literal("§6ZEUS §fha convocado la ira de los dioses."));
            level.playSound(null, center, SoundEvents.BEACON_ACTIVATE, SoundSource.MASTER, 3.0F, 0.55F);
            spawnRing(5.5, 48, Blocks.CRYING_OBSIDIAN.defaultBlockState(), Blocks.AMETHYST_BLOCK.defaultBlockState());
            spawnRing(8.0, 64, Blocks.GOLD_BLOCK.defaultBlockState(), Blocks.QUARTZ_BLOCK.defaultBlockState());
            spawnVerticalRunes();
        }

        boolean tick() {
            tick++;

            if (player.isRemoved() || !player.isAlive()) {
                return true;
            }

            animateOrbiters();
            updateRelics();

            if (tick == 35) {
                level.playSound(null, center, SoundEvents.PORTAL_TRIGGER, SoundSource.MASTER, 2.5F, 0.65F);
                spawnRing(11.0, 72, Blocks.OBSIDIAN.defaultBlockState(), Blocks.SEA_LANTERN.defaultBlockState());
                spawnOrbiters();
            }

            if (tick == 80) {
                announce("§eZEUS", "§fEl portador del rayo desciende.");
                buildZeus();
                thunderCrown(12, 7.0, true);
            }

            if (tick == 125) {
                announce("§cTHOR", "§fEl martillo del trueno responde al llamado.");
                buildThor();
                thunderCrown(8, 10.0, true);
            }

            if (tick == 165) {
                announce("§3POSEIDÓN", "§fEl océano se levanta contra el objetivo.");
                buildPoseidon();
                waterBurst();
            }

            if (tick == 205) {
                announce("§6RA", "§fEl sol abre su ojo sobre el ritual.");
                buildRa();
                solarBurst();
            }

            if (tick == 245) {
                announce("§5HADES", "§fLas puertas del inframundo se han abierto.");
                buildHades();
                underworldBurst();
            }

            if (tick == 275) {
                announce("§bPRIMER JUICIO", "§fLos dioses comienzan el castigo.");
                spawnRelic("ZEUS", new Vec3(center.getX() + 0.5, center.getY() + 24, center.getZ() + 0.5),
                    Blocks.LIGHTNING_ROD.defaultBlockState(), Blocks.GOLD_BLOCK.defaultBlockState(), -0.62);
            }

            if (tick == 330) {
                spawnRelic("THOR", new Vec3(center.getX() + 5.5, center.getY() + 26, center.getZ() - 4.5),
                    Blocks.COPPER_BLOCK.defaultBlockState(), Blocks.ANVIL.defaultBlockState(), -0.72);
            }

            if (tick == 385) {
                spawnRelic("POSEIDON", new Vec3(center.getX() - 6.5, center.getY() + 25, center.getZ() + 4.5),
                    Blocks.PRISMARINE.defaultBlockState(), Blocks.SEA_LANTERN.defaultBlockState(), -0.68);
            }

            if (tick == 435) {
                spawnRelic("RA", new Vec3(center.getX() + 2.5, center.getY() + 28, center.getZ() + 7.5),
                    Blocks.GOLD_BLOCK.defaultBlockState(), Blocks.GLOWSTONE.defaultBlockState(), -0.78);
            }

            if (tick == 485) {
                spawnRelic("HADES", new Vec3(center.getX() - 3.5, center.getY() + 27, center.getZ() - 7.5),
                    Blocks.CRYING_OBSIDIAN.defaultBlockState(), Blocks.SOUL_LANTERN.defaultBlockState(), -0.76);
            }

            if (tick >= 285 && tick <= 560) {
                applyTotemSchedule();
            }

            if (tick == 525) {
                announce("§4IRA ABSOLUTA", "§fZEUS ordena el golpe de todos los dioses.");
                level.playSound(null, center, SoundEvents.WITHER_SPAWN, SoundSource.MASTER, 4.0F, 0.6F);
                thunderCrown(24, 14.0, false);
                prepareCrater();
            }

            if (craterPrepared && !craterQueue.isEmpty()) {
                processCrater(220);
            } else if (craterPrepared && !craterDecorated) {
                decorateCrater();
                craterDecorated = true;
            }

            if (tick >= 600 && tick % 4 == 0) {
                level.sendParticles(ParticleTypes.CLOUD,
                    center.getX() + 0.5, center.getY() + 2.0, center.getZ() + 0.5,
                    55, 8.0, 3.0, 8.0, 0.08);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    center.getX() + 0.5, center.getY() + 4.0, center.getZ() + 0.5,
                    38, 8.0, 5.0, 8.0, 0.16);
            }

            if (tick == 675) {
                player.sendSystemMessage(Component.literal("§6✦ §fEL JUICIO DE LOS DIOSES HA TERMINADO §6✦"));
            }

            return tick > 700 && craterQueue.isEmpty() && relics.isEmpty();
        }

        private void announce(String title, String subtitle) {
            player.sendSystemMessage(Component.literal("§l" + title + " §r" + subtitle));
            level.playSound(null, center, SoundEvents.BEACON_POWER_SELECT, SoundSource.MASTER, 2.0F, 0.75F);
        }

        private void spawnVerticalRunes() {
            BlockState[] states = {
                Blocks.AMETHYST_BLOCK.defaultBlockState(),
                Blocks.SEA_LANTERN.defaultBlockState(),
                Blocks.GLOWSTONE.defaultBlockState(),
                Blocks.CRYING_OBSIDIAN.defaultBlockState()
            };
            for (int i = 0; i < 20; i++) {
                double a = (Math.PI * 2.0 * i) / 20.0;
                double r = 9.5;
                for (int y = 1; y <= 4; y++) {
                    spawnDisplay(center.getX() + 0.5 + Math.cos(a) * r,
                        center.getY() + y,
                        center.getZ() + 0.5 + Math.sin(a) * r,
                        states[(i + y) % states.length], null, false);
                }
            }
        }

        private void spawnRing(double radius, int points, BlockState a, BlockState b) {
            for (int i = 0; i < points; i++) {
                double angle = Math.PI * 2.0 * i / points;
                double x = center.getX() + 0.5 + Math.cos(angle) * radius;
                double z = center.getZ() + 0.5 + Math.sin(angle) * radius;
                spawnDisplay(x, center.getY() + 0.15, z, (i % 2 == 0 ? a : b), null, false);
            }
        }

        private void spawnOrbiters() {
            BlockState[] states = {
                Blocks.GOLD_BLOCK.defaultBlockState(),
                Blocks.AMETHYST_BLOCK.defaultBlockState(),
                Blocks.SEA_LANTERN.defaultBlockState(),
                Blocks.COPPER_BLOCK.defaultBlockState(),
                Blocks.CRYING_OBSIDIAN.defaultBlockState()
            };
            for (int i = 0; i < 15; i++) {
                UUID id = spawnDisplay(center.getX() + 0.5, center.getY() + 5.0, center.getZ() + 0.5,
                    states[i % states.length], null, false);
                if (id != null) {
                    orbiters.add(id);
                }
            }
        }

        private void animateOrbiters() {
            if (orbiters.isEmpty()) return;
            for (int i = 0; i < orbiters.size(); i++) {
                Entity e = level.getEntity(orbiters.get(i));
                if (e == null) continue;
                double angle = tick * 0.035 + (Math.PI * 2.0 * i / orbiters.size());
                double radius = 8.5 + Math.sin(tick * 0.025 + i) * 2.0;
                double y = center.getY() + 5.0 + Math.sin(angle * 2.0) * 2.2;
                e.setPos(center.getX() + 0.5 + Math.cos(angle) * radius, y,
                    center.getZ() + 0.5 + Math.sin(angle) * radius);
            }
        }

        private void buildZeus() {
            Vec3 o = offset(0, 0, -10);
            buildGod("§eZEUS", o, Blocks.QUARTZ_BLOCK.defaultBlockState(),
                Blocks.GOLD_BLOCK.defaultBlockState(), Blocks.BLUE_STAINED_GLASS.defaultBlockState());
            for (int y = 1; y <= 6; y++) {
                spawnDisplay(o.x + 3, o.y + y, o.z, Blocks.LIGHTNING_ROD.defaultBlockState(), null, false);
            }
            spawnDisplay(o.x + 3, o.y + 7, o.z, Blocks.GOLD_BLOCK.defaultBlockState(), null, false);
        }

        private void buildThor() {
            Vec3 o = offset(9, 0, -5);
            buildGod("§cTHOR", o, Blocks.STONE_BRICKS.defaultBlockState(),
                Blocks.RED_WOOL.defaultBlockState(), Blocks.COPPER_BLOCK.defaultBlockState());
            for (int y = 1; y <= 4; y++) {
                spawnDisplay(o.x - 3, o.y + y, o.z, Blocks.COPPER_BLOCK.defaultBlockState(), null, false);
            }
            for (int x = -1; x <= 1; x++) {
                spawnDisplay(o.x - 3 + x, o.y + 5, o.z, Blocks.ANVIL.defaultBlockState(), null, false);
            }
        }

        private void buildPoseidon() {
            Vec3 o = offset(-9, 0, -5);
            buildGod("§3POSEIDÓN", o, Blocks.PRISMARINE.defaultBlockState(),
                Blocks.DARK_PRISMARINE.defaultBlockState(), Blocks.SEA_LANTERN.defaultBlockState());
            for (int y = 1; y <= 7; y++) {
                spawnDisplay(o.x - 3, o.y + y, o.z, Blocks.PRISMARINE.defaultBlockState(), null, false);
            }
            for (int x = -1; x <= 1; x++) {
                spawnDisplay(o.x - 3 + x, o.y + 8, o.z, Blocks.SEA_LANTERN.defaultBlockState(), null, false);
            }
        }

        private void buildRa() {
            Vec3 o = offset(8, 0, 7);
            buildGod("§6RA", o, Blocks.GOLD_BLOCK.defaultBlockState(),
                Blocks.YELLOW_CONCRETE.defaultBlockState(), Blocks.ORANGE_STAINED_GLASS.defaultBlockState());
            for (int i = 0; i < 12; i++) {
                double a = i * Math.PI * 2.0 / 12.0;
                spawnDisplay(o.x + Math.cos(a) * 3.0, o.y + 7.5 + Math.sin(a) * 3.0, o.z,
                    Blocks.GLOWSTONE.defaultBlockState(), null, false);
            }
        }

        private void buildHades() {
            Vec3 o = offset(-8, 0, 7);
            buildGod("§5HADES", o, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(),
                Blocks.CRYING_OBSIDIAN.defaultBlockState(), Blocks.PURPLE_STAINED_GLASS.defaultBlockState());
            for (int y = 1; y <= 6; y++) {
                spawnDisplay(o.x + 3, o.y + y, o.z, Blocks.BLACKSTONE.defaultBlockState(), null, false);
            }
            for (int i = 0; i < 5; i++) {
                spawnDisplay(o.x + 3 + i, o.y + 7, o.z, Blocks.CRYING_OBSIDIAN.defaultBlockState(), null, false);
            }
        }

        private Vec3 offset(double x, double y, double z) {
            return new Vec3(center.getX() + 0.5 + x, center.getY() + y, center.getZ() + 0.5 + z);
        }

        private void buildGod(String name, Vec3 o, BlockState body, BlockState accent, BlockState aura) {
            for (int y = 1; y <= 4; y++) {
                for (int x = -1; x <= 1; x++) {
                    spawnDisplay(o.x + x, o.y + y, o.z, (x == 0 ? body : accent), null, false);
                }
            }
            for (int x = -1; x <= 1; x++) {
                spawnDisplay(o.x + x, o.y + 5, o.z, body, null, false);
                UUID head = spawnDisplay(o.x + x, o.y + 6, o.z, aura, (x == 0 ? name : null), x == 0);
                if (head == null) break;
            }
            for (int y = 1; y <= 3; y++) {
                spawnDisplay(o.x - 1, o.y - y + 1, o.z, body, null, false);
                spawnDisplay(o.x + 1, o.y - y + 1, o.z, body, null, false);
            }
            for (int x = 2; x <= 4; x++) {
                spawnDisplay(o.x + x, o.y + 4, o.z, accent, null, false);
                spawnDisplay(o.x - x, o.y + 4, o.z, accent, null, false);
            }
            for (int i = 0; i < 10; i++) {
                double a = i * Math.PI * 2.0 / 10.0;
                spawnDisplay(o.x + Math.cos(a) * 3.4, o.y + 3.8 + Math.sin(a) * 2.4, o.z + 1.2,
                    aura, null, false);
            }
        }

        private UUID spawnDisplay(double x, double y, double z, BlockState state, String name, boolean showName) {
            Entity created = EntityType.BLOCK_DISPLAY.create(level);
            if (!(created instanceof Display.BlockDisplay display)) {
                return null;
            }
            CompoundTag displayTag = new CompoundTag();
            display.saveWithoutId(displayTag);
            displayTag.put("block_state", NbtUtils.writeBlockState(state));
            display.load(displayTag);
            display.setPos(x, y, z);
            if (name != null) {
                display.setCustomName(Component.literal(name));
                display.setCustomNameVisible(showName);
            }
            level.addFreshEntity(display);
            displays.add(display.getUUID());
            return display.getUUID();
        }

        private void thunderCrown(int count, double radius, boolean visualOnly) {
            for (int i = 0; i < count; i++) {
                double a = Math.PI * 2.0 * i / count;
                double r = radius * (0.55 + (i % 3) * 0.22);
                BlockPos p = BlockPos.containing(
                    center.getX() + 0.5 + Math.cos(a) * r,
                    center.getY(),
                    center.getZ() + 0.5 + Math.sin(a) * r
                );
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null) {
                    bolt.moveTo(Vec3.atBottomCenterOf(p));
                    bolt.setVisualOnly(visualOnly);
                    level.addFreshEntity(bolt);
                }
            }
        }

        private void waterBurst() {
            level.sendParticles(ParticleTypes.SPLASH,
                center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5,
                180, 7.0, 4.0, 7.0, 0.35);
            level.sendParticles(ParticleTypes.BUBBLE_POP,
                center.getX() + 0.5, center.getY() + 2.0, center.getZ() + 0.5,
                120, 5.0, 3.0, 5.0, 0.25);
        }

        private void solarBurst() {
            level.sendParticles(ParticleTypes.FLAME,
                center.getX() + 0.5, center.getY() + 5.0, center.getZ() + 0.5,
                170, 7.0, 6.0, 7.0, 0.18);
            level.sendParticles(ParticleTypes.END_ROD,
                center.getX() + 0.5, center.getY() + 5.0, center.getZ() + 0.5,
                100, 5.5, 5.5, 5.5, 0.1);
        }

        private void underworldBurst() {
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                center.getX() + 0.5, center.getY() + 2.0, center.getZ() + 0.5,
                160, 7.0, 3.0, 7.0, 0.12);
            level.sendParticles(ParticleTypes.PORTAL,
                center.getX() + 0.5, center.getY() + 3.0, center.getZ() + 0.5,
                120, 6.0, 5.0, 6.0, 0.35);
        }

        private void spawnRelic(String god, Vec3 start, BlockState body, BlockState core, double speedY) {
            List<UUID> parts = new ArrayList<>();
            for (int y = 0; y < 7; y++) {
                UUID id = spawnDisplay(start.x, start.y + y, start.z, body, null, false);
                if (id != null) parts.add(id);
            }
            for (int x = -1; x <= 1; x++) {
                UUID id = spawnDisplay(start.x + x, start.y + 7, start.z, core, null, false);
                if (id != null) parts.add(id);
            }
            for (int z = -1; z <= 1; z++) {
                UUID id = spawnDisplay(start.x, start.y + 6, start.z + z, core, null, false);
                if (id != null) parts.add(id);
            }
            relics.add(new Relic(god, parts, speedY, 130));
            level.playSound(null, center, SoundEvents.TRIDENT_THUNDER, SoundSource.MASTER, 3.2F, 0.6F);
        }

        private void updateRelics() {
            Iterator<Relic> it = relics.iterator();
            while (it.hasNext()) {
                Relic relic = it.next();
                relic.life--;

                double lowestY = Double.MAX_VALUE;
                for (UUID id : relic.parts) {
                    Entity e = level.getEntity(id);
                    if (e == null) continue;
                    e.setPos(e.getX(), e.getY() + relic.speedY, e.getZ());
                    lowestY = Math.min(lowestY, e.getY());
                }

                if (lowestY <= center.getY() + 1.2 || relic.life <= 0) {
                    divineImpact(relic.god);
                    for (UUID id : relic.parts) {
                        Entity e = level.getEntity(id);
                        if (e != null) e.discard();
                    }
                    it.remove();
                }
            }
        }

        private void divineImpact(String god) {
            level.playSound(null, center, SoundEvents.GENERIC_EXPLODE, SoundSource.MASTER, 4.0F, 0.65F);
            level.sendParticles(ParticleTypes.EXPLOSION,
                center.getX() + 0.5, center.getY() + 1.5, center.getZ() + 0.5,
                14, 5.0, 2.0, 5.0, 0.0);
            level.sendParticles(ParticleTypes.CLOUD,
                center.getX() + 0.5, center.getY() + 1.2, center.getZ() + 0.5,
                130, 6.0, 2.2, 6.0, 0.12);

            if (god.equals("ZEUS") || god.equals("THOR")) {
                thunderCrown(10, 8.0, false);
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    center.getX() + 0.5, center.getY() + 2.5, center.getZ() + 0.5,
                    160, 6.0, 4.0, 6.0, 0.24);
            } else if (god.equals("POSEIDON")) {
                waterBurst();
            } else if (god.equals("RA")) {
                solarBurst();
            } else {
                underworldBurst();
            }
        }

        private void applyTotemSchedule() {
            int windowStart = 285;
            int windowEnd = 560;
            int elapsed = tick - windowStart + 1;
            int totalWindow = windowEnd - windowStart + 1;
            int desired = (int) Math.floor((elapsed / (double) totalWindow) * requestedTotems);
            int missing = Math.min(32, desired - totemsTriggered);

            for (int i = 0; i < missing; i++) {
                if (!player.isAlive()) return;
                player.invulnerableTime = 0;
                player.hurt(player.damageSources().lightningBolt(), Float.MAX_VALUE);
                player.invulnerableTime = 0;
                totemsTriggered++;
            }
        }

        private void prepareCrater() {
            if (craterPrepared) return;
            craterPrepared = true;
            int radius = 15;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist > radius) continue;
                    int depth = 2 + (int) Math.round((1.0 - dist / radius) * 6.0);
                    for (int d = 0; d <= depth; d++) {
                        craterQueue.add(center.offset(dx, -d, dz));
                    }
                }
            }
        }

        private void processCrater(int maxPerTick) {
            int processed = 0;
            while (processed < maxPerTick && !craterQueue.isEmpty()) {
                BlockPos p = craterQueue.poll();
                if (p == null) break;
                BlockState state = level.getBlockState(p);
                if (!state.is(Blocks.BEDROCK)) {
                    level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
                }
                processed++;
            }

            if (tick % 2 == 0) {
                level.sendParticles(ParticleTypes.LARGE_SMOKE,
                    center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
                    80, 9.0, 3.5, 9.0, 0.08);
                level.sendParticles(ParticleTypes.FLAME,
                    center.getX() + 0.5, center.getY() - 1.0, center.getZ() + 0.5,
                    55, 8.0, 2.5, 8.0, 0.07);
            }
        }

        private void decorateCrater() {
            int radius = 14;
            for (int i = 0; i < 220; i++) {
                int dx = level.getRandom().nextInt(radius * 2 + 1) - radius;
                int dz = level.getRandom().nextInt(radius * 2 + 1) - radius;
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > radius) continue;
                int depth = 2 + (int) Math.round((1.0 - dist / 15.0) * 6.0);
                BlockPos p = center.offset(dx, -depth - 1, dz);
                if (level.getBlockState(p).is(Blocks.BEDROCK)) continue;

                int pick = level.getRandom().nextInt(10);
                BlockState deco = pick < 3 ? Blocks.MAGMA_BLOCK.defaultBlockState()
                    : pick < 6 ? Blocks.OBSIDIAN.defaultBlockState()
                    : pick < 8 ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                    : Blocks.BLACKSTONE.defaultBlockState();
                level.setBlock(p, deco, 3);

                if (pick == 0 && level.getBlockState(p.above()).isAir()) {
                    level.setBlock(p.above(), Blocks.LAVA.defaultBlockState(), 3);
                }
            }

            for (int i = 0; i < 22; i++) {
                double angle = i * Math.PI * 2.0 / 22.0;
                double r = 13.0;
                BlockPos rim = BlockPos.containing(center.getX() + Math.cos(angle) * r,
                    center.getY(), center.getZ() + Math.sin(angle) * r);
                level.setBlock(rim, i % 3 == 0 ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                    : Blocks.OBSIDIAN.defaultBlockState(), 3);
            }
        }

        void cleanup() {
            for (UUID id : displays) {
                Entity e = level.getEntity(id);
                if (e != null) {
                    e.discard();
                }
            }
            displays.clear();
            orbiters.clear();

            for (Relic relic : relics) {
                for (UUID id : relic.parts) {
                    Entity e = level.getEntity(id);
                    if (e != null) e.discard();
                }
            }
            relics.clear();
            craterQueue.clear();
        }
    }

    private static final class Relic {
        final String god;
        final List<UUID> parts;
        final double speedY;
        int life;

        Relic(String god, List<UUID> parts, double speedY, int life) {
            this.god = god;
            this.parts = parts;
            this.speedY = speedY;
            this.life = life;
        }
    }
}
