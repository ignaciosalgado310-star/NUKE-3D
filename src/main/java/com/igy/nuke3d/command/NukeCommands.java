package com.igy.nuke3d.command;

import com.igy.nuke3d.Nuke3D;
import com.igy.nuke3d.config.NukeConfig;
import com.igy.nuke3d.disaster.NukeManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Nuke3D.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NukeCommands {
    private static final String ALLOWED_TARGET = "Nelonino";
    private static final int MAX_TOTEMS = 10000;

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        var target = Commands.argument("target", EntityArgument.player())
                .executes(context -> startAtPlayer(context, null, null))
                .then(Commands.argument("totems", IntegerArgumentType.integer(1, MAX_TOTEMS))
                        .executes(context -> startAtPlayer(
                                context,
                                IntegerArgumentType.getInteger(context, "totems"),
                                null
                        ))
                        .then(Commands.argument("damage_hearts", DoubleArgumentType.doubleArg(0.0, 50000.0))
                                .executes(context -> startAtPlayer(
                                        context,
                                        IntegerArgumentType.getInteger(context, "totems"),
                                        DoubleArgumentType.getDouble(context, "damage_hearts")
                                ))));

        dispatcher.register(
                Commands.literal("destruction")
                        .requires(source -> source.hasPermission(NukeConfig.COMMAND_PERMISSION_LEVEL.get()))
                        .then(Commands.literal("nuke")
                                .then(Commands.literal("player").then(target)))
        );
    }

    private static int startAtPlayer(CommandContext<CommandSourceStack> context, Integer hits, Double damage) {
        try {
            ServerPlayer target = EntityArgument.getPlayer(context, "target");
            String name = target.getGameProfile().getName();
            if (!ALLOWED_TARGET.equalsIgnoreCase(name)) {
                context.getSource().sendFailure(Component.literal(
                        "§cNUKE 3D está bloqueado: solo puede activarse para " + ALLOWED_TARGET + "."));
                return 0;
            }

            if (!NukeManager.start(
                    target.serverLevel(),
                    target.position(),
                    hits,
                    damage,
                    target.getUUID())) {
                context.getSource().sendFailure(Component.literal("§cYa hay demasiados NUKE activos."));
                return 0;
            }

            int requested = hits == null ? NukeConfig.DAMAGE_PULSES.get() : hits;
            context.getSource().sendSuccess(() -> Component.literal(
                    "§aNUKE 3D: §fNUKE §7activado sobre §f" + ALLOWED_TARGET
                            + " §7| tótems PURPURE-style: §f" + requested
                            + " §7| 1 tótem cada 2 ticks."), true);
            return 1;
        } catch (Exception exception) {
            context.getSource().sendFailure(Component.literal(
                    "§cNo se pudo encontrar a " + ALLOWED_TARGET + " en línea."));
            return 0;
        }
    }

    private NukeCommands() {}
}
