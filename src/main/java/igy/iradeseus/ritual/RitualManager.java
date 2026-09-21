package igy.iradeseus.ritual;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import igy.iradeseus.IraDeSeus3D;
import igy.iradeseus.entity.DivineProjectileEntity;
import igy.iradeseus.entity.MythicAvatarEntity;
import igy.iradeseus.entity.RitualFxEntity;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = IraDeSeus3D.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RitualManager {
    private static final Map<UUID, Ritual> ACTIVE = new HashMap<>();

    private RitualManager() {}

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
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
                            ctx.getSource().sendSuccess(() -> Component.literal(
                                "§5[IRA-DE-SEUS] §fRitual mitológico iniciado sobre §e" +
                                    player.getGameProfile().getName() + " §fcon §b" + totems + " §ftótems."), true);
                            return 1;
                        })
                    )
                )
        );
    }

    private static void start(ServerPlayer player, int totems) {
        Ritual previous = ACTIVE.remove(player.getUUID());
        if (previous != null) previous.cleanup();

        Ritual ritual = new Ritual(player, totems);
        ACTIVE.put(player.getUUID(), ritual);
        ritual.begin();
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ACTIVE.isEmpty()) return;

        Iterator<Map.Entry<UUID, Ritual>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Ritual ritual = it.next().getValue();
            if (ritual.tick()) {
                ritual.cleanup();
                it.remove();
            }
        }
    }

    private static final class Ritual {
        private final ServerPlayer player;
        private final ServerLevel level;
        private final Vec3 center;
        private final BlockPos craterCenter;
        private final int requestedTotems;
        private final List<AvatarRef> avatars = new ArrayList<>();
        private final List<UUID> persistentFx = new ArrayList<>();
        private final Queue<BlockPos> crater = new ArrayDeque<>();

        private int tick;
        private int consumed;
        private boolean finaleStarted;
        private boolean craterPrepared;

        Ritual(ServerPlayer player, int requestedTotems) {
            this.player = player;
            this.level = (ServerLevel) player.level();
            this.center = player.position();
            this.craterCenter = player.blockPosition();
            this.requestedTotems = requestedTotems;
        }

        void begin() {
            player.sendSystemMessage(Component.literal("§5✦ §dLOS CIELOS SE ABREN §5✦"));
            player.sendSystemMessage(Component.literal("§fNo es una construcción: §6los dioses han tomado forma."));
            level.playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_SPAWN,
                SoundSource.MASTER, 3.0F, 0.55F);

            persistentFx.add(spawnFx(0, 0x9D7BFF, 7.5F, 900, 0.75F,
                center.add(0, 0.15, 0)));
            persistentFx.add(spawnFx(1, 0x4D77FF, 5.5F, 900, -0.55F,
                center.add(0, 5.2, 0)));
            persistentFx.add(spawnFx(7, 0xFFE06B, 6.2F, 900, 0.33F,
                center.add(0, 12.0, 0)));
        }

        boolean tick() {
            tick++;

            if (player.isRemoved() || !player.isAlive()) return true;

            animateAvatars();
            ambientParticles();

            if (tick == 35) {
                announce("§eZEUS", "§fEl soberano del rayo desciende.");
                spawnAvatar(0, -Math.PI / 2.0);
                spawnFx(2, 0x9FD9FF, 3.4F, 180, 1.25F, center.add(0, 5.5, -9.5));
                lightningFan(9, 8.0, true);
            }

            if (tick == 78) {
                announce("§cTHOR", "§fEl martillo rompe el silencio.");
                spawnAvatar(1, -0.25);
                spawnFx(2, 0x68C7FF, 3.2F, 180, -1.05F, center.add(8.8, 5.0, -4.0));
            }

            if (tick == 121) {
                announce("§3POSEIDÓN", "§fEl tridente reclama el campo.");
                spawnAvatar(2, Math.PI);
                spawnFx(3, 0x47E7FF, 3.7F, 170, 0.65F, center.add(-9.3, 4.5, 0.8));
            }

            if (tick == 164) {
                announce("§6RA", "§fEl ojo solar fija su objetivo.");
                spawnAvatar(3, 0.65);
                spawnFx(3, 0xFFB629, 4.0F, 190, -0.8F, center.add(7.0, 7.2, 6.5));
            }

            if (tick == 207) {
                announce("§5HADES", "§fEl inframundo abre sus puertas.");
                spawnAvatar(4, 2.2);
                spawnFx(1, 0xA34DFF, 4.3F, 220, 0.55F, center.add(-6.5, 4.8, 7.1));
            }

            if (tick == 245) {
                player.sendSystemMessage(Component.literal("§b✦ LOS CINCO JUICIOS HAN SIDO INVOCADOS ✦"));
                level.playSound(null, player.blockPosition(), SoundEvents.WITHER_SPAWN,
                    SoundSource.MASTER, 3.2F, 0.75F);
                spawnFx(4, 0xBBDFFF, 8.0F, 260, 1.1F, center.add(0, 4.0, 0));
            }

            if (tick == 270) launchVolley(0, 3, 1.4F);
            if (tick == 325) launchVolley(1, 4, 1.35F);
            if (tick == 380) launchVolley(2, 4, 1.45F);
            if (tick == 435) launchVolley(3, 5, 1.55F);
            if (tick == 490) launchVolley(4, 5, 1.5F);

            if (tick >= 255 && tick <= 720) {
                consumeTotems();
            }

            if (tick >= 510 && tick < 650 && tick % 18 == 0) {
                int variant = (tick / 18) % 5;
                launchVolley(variant, 2 + (tick % 3), 1.25F + (tick % 4) * 0.08F);
                spawnFx(5, palette(variant), 4.0F + (tick % 5) * 0.45F,
                    70, 1.4F, center.add(0, 1.0 + (tick % 3), 0));
            }

            if (tick >= 525 && tick <= 645 && tick % 12 == 0) {
                lightningFan(4, 10.0 + (tick % 4), tick < 620);
            }

            if (tick == 650) {
                startFinale();
            }

            if (finaleStarted && tick >= 650 && tick <= 735 && tick % 5 == 0) {
                level.sendParticles(IraDeSeus3D.DIVINE_MOTE.get(),
                    center.x, center.y + 5.5, center.z,
                    45, 6.5, 5.5, 6.5, 0.18);
                level.sendParticles(IraDeSeus3D.ARC_SPARK.get(),
                    center.x, center.y + 4.0, center.z,
                    65, 8.0, 5.0, 8.0, 0.22);
            }

            if (tick == 690) {
                prepareCrater();
            }

            if (craterPrepared && !crater.isEmpty()) {
                processCrater(260);
            }

            if (tick == 760) {
                spawnFx(5, 0xFFFFFF, 13.0F, 130, -1.6F, center.add(0, 1.0, 0));
                lightningFan(18, 15.0, false);
                level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE,
                    SoundSource.MASTER, 4.0F, 0.52F);
            }

            if (tick == 830) {
                player.sendSystemMessage(Component.literal("§6✦ §fLA IRA DE LOS DIOSES HA TERMINADO §6✦"));
            }

            return tick > 880 && crater.isEmpty();
        }

        private void announce(String god, String line) {
            player.sendSystemMessage(Component.literal("§l" + god + " §r" + line));
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT,
                SoundSource.MASTER, 2.1F, 0.72F);
        }

        private void spawnAvatar(int variant, double angle) {
            MythicAvatarEntity avatar = IraDeSeus3D.MYTHIC_AVATAR.get().create(level);
            if (avatar == null) return;

            avatar.setVariant(variant);
            avatar.setPower(1.0F);
            double radius = 9.5;
            avatar.setPos(center.x + Math.cos(angle) * radius,
                center.y + 0.8,
                center.z + Math.sin(angle) * radius);
            avatar.setYRot((float) Math.toDegrees(-angle + Math.PI / 2.0));
            level.addFreshEntity(avatar);
            avatars.add(new AvatarRef(avatar.getUUID(), variant, angle));
        }

        private void animateAvatars() {
            for (AvatarRef ref : avatars) {
                Entity entity = level.getEntity(ref.id);
                if (!(entity instanceof MythicAvatarEntity avatar)) continue;

                double sway = Math.sin((tick + ref.variant * 17) * 0.018) * 0.55;
                double angle = ref.baseAngle + sway * 0.08;
                double radius = 9.5 + Math.sin((tick + ref.variant * 31) * 0.025) * 0.45;
                double y = center.y + 0.75 + Math.sin((tick + ref.variant * 19) * 0.045) * 0.42;

                avatar.setPos(center.x + Math.cos(angle) * radius, y,
                    center.z + Math.sin(angle) * radius);
                avatar.setYRot((float) Math.toDegrees(-angle + Math.PI / 2.0));
                avatar.setPower(finaleStarted ? 1.55F : 1.0F + (float) Math.sin(tick * 0.03) * 0.08F);
                avatar.hurtMarked = true;
            }
        }

        private void ambientParticles() {
            if (tick % 4 != 0) return;

            double radius = 4.5 + Math.sin(tick * 0.035) * 1.8;
            double a = tick * 0.12;
            double px = center.x + Math.cos(a) * radius;
            double pz = center.z + Math.sin(a) * radius;

            level.sendParticles(IraDeSeus3D.DIVINE_MOTE.get(),
                px, center.y + 1.5 + Math.sin(a * 0.6) * 2.0, pz,
                8, 0.45, 0.6, 0.45, 0.025);

            if (tick > 220) {
                level.sendParticles(IraDeSeus3D.ARC_SPARK.get(),
                    center.x, center.y + 4.0, center.z,
                    9, 5.5, 3.5, 5.5, 0.035);
            }
        }

        private void launchVolley(int variant, int count, float scale) {
            AvatarRef ref = avatars.stream().filter(a -> a.variant == variant).findFirst().orElse(null);
            Vec3 source = ref != null && level.getEntity(ref.id) != null
                ? level.getEntity(ref.id).position().add(0, 4.0, 0)
                : center.add(0, 18.0, 0);

            for (int i = 0; i < count; i++) {
                double spread = (i - (count - 1) / 2.0) * 0.9;
                Vec3 start = source.add(spread, 7.0 + i * 0.7, -spread * 0.45);
                Vec3 target = player.position().add(0,
                    0.6 + (i % 2) * 0.35,
                    0);

                DivineProjectileEntity projectile = IraDeSeus3D.DIVINE_PROJECTILE.get().create(level);
                if (projectile == null) continue;

                projectile.setPos(start.x, start.y, start.z);
                projectile.configure(variant, scale + i * 0.06F, target);
                level.addFreshEntity(projectile);
            }

            spawnFx(6, palette(variant), 2.8F + count * 0.15F, 85, 1.1F, source);
            level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THUNDER,
                SoundSource.MASTER, 2.2F, 0.62F + variant * 0.06F);
        }

        private void consumeTotems() {
            int start = 255;
            int end = 720;
            int elapsed = tick - start + 1;
            int window = end - start + 1;
            int shouldHaveConsumed = (int) Math.floor((elapsed / (double) window) * requestedTotems);
            int due = Math.min(24, shouldHaveConsumed - consumed);

            for (int i = 0; i < due; i++) {
                if (!player.isAlive()) return;
                player.invulnerableTime = 0;
                player.hurt(player.damageSources().lightningBolt(), Float.MAX_VALUE);
                player.invulnerableTime = 0;
                consumed++;
            }
        }

        private void startFinale() {
            finaleStarted = true;
            player.sendSystemMessage(Component.literal("§4§lIRA ABSOLUTA §r§f— los cinco dioses atacan al mismo tiempo."));
            level.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.MASTER, 4.0F, 0.58F);

            persistentFx.add(spawnFx(6, 0xD7B5FF, 10.5F, 240, 2.0F, center.add(0, 5.0, 0)));
            persistentFx.add(spawnFx(7, 0xFFF0A2, 12.0F, 240, -0.9F, center.add(0, 13.0, 0)));

            for (int variant = 0; variant < 5; variant++) {
                launchVolley(variant, 4, 1.75F);
            }
            lightningFan(28, 14.0, false);
        }

        private UUID spawnFx(int variant, int color, float scale, int lifetime, float spin, Vec3 pos) {
            RitualFxEntity fx = IraDeSeus3D.RITUAL_FX.get().create(level);
            if (fx == null) return new UUID(0L, 0L);

            fx.configure(variant, color, scale, lifetime, spin);
            fx.setPos(pos.x, pos.y, pos.z);
            level.addFreshEntity(fx);
            return fx.getUUID();
        }

        private int palette(int variant) {
            return switch (variant) {
                case 0 -> 0xB9E4FF;
                case 1 -> 0x6FC9FF;
                case 2 -> 0x35E5FF;
                case 3 -> 0xFFC43D;
                case 4 -> 0xB15CFF;
                default -> 0xFFFFFF;
            };
        }

        private void lightningFan(int count, double radius, boolean visualOnly) {
            for (int i = 0; i < count; i++) {
                double a = (Math.PI * 2.0 * i / count) + tick * 0.013;
                double r = radius * (0.45 + (i % 4) * 0.16);
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt == null) continue;

                bolt.moveTo(center.x + Math.cos(a) * r, center.y, center.z + Math.sin(a) * r);
                bolt.setVisualOnly(visualOnly);
                level.addFreshEntity(bolt);
            }
        }

        private void prepareCrater() {
            if (craterPrepared) return;
            craterPrepared = true;

            int radius = 18;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double dist = Math.sqrt(dx * dx + dz * dz);
                    if (dist > radius) continue;

                    double normalized = 1.0 - dist / radius;
                    int depth = 2 + (int) Math.floor(normalized * normalized * 9.0);
                    for (int d = 0; d <= depth; d++) {
                        crater.add(craterCenter.offset(dx, -d, dz));
                    }
                }
            }
        }

        private void processCrater(int budget) {
            int changed = 0;
            while (changed < budget && !crater.isEmpty()) {
                BlockPos pos = crater.poll();
                if (pos == null) break;

                BlockState state = level.getBlockState(pos);
                if (!state.isAir() && !state.is(Blocks.BEDROCK)) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
                changed++;
            }

            if (tick % 2 == 0) {
                level.sendParticles(IraDeSeus3D.DIVINE_MOTE.get(),
                    craterCenter.getX() + 0.5, craterCenter.getY() + 1.0, craterCenter.getZ() + 0.5,
                    80, 9.0, 4.0, 9.0, 0.22);
                level.sendParticles(IraDeSeus3D.ARC_SPARK.get(),
                    craterCenter.getX() + 0.5, craterCenter.getY() + 2.0, craterCenter.getZ() + 0.5,
                    95, 10.0, 5.0, 10.0, 0.25);
            }
        }

        void cleanup() {
            for (AvatarRef ref : avatars) {
                Entity entity = level.getEntity(ref.id);
                if (entity != null) entity.discard();
            }
            avatars.clear();

            for (UUID id : persistentFx) {
                Entity entity = level.getEntity(id);
                if (entity != null) entity.discard();
            }
            persistentFx.clear();
            crater.clear();
        }
    }

    private record AvatarRef(UUID id, int variant, double baseAngle) {}
}
