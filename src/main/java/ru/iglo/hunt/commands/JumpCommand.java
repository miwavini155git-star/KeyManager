package ru.iglo.hunt.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.GameType;
import ru.iglo.hunt.managers.JumpManager;

public class JumpCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("jump")
                        .requires(source -> {
                            // Только Creative mode (gamemode 1)
                            if (source.getEntity() instanceof ServerPlayerEntity) {
                                ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
                                return player.gameMode.getGameModeForPlayer() == GameType.CREATIVE;
                            }
                            return false;
                        })
                        .then(Commands.literal("toggle")
                                .executes(context -> {
                                    JumpManager.toggleJump();
                                    String status = JumpManager.isJumpEnabled() ? "§aвключен" : "§cотключен";
                                    context.getSource().sendSuccess(
                                            new StringTextComponent("Прыжок " + status),
                                            true
                                    );
                                    return 1;
                                }))
                        .executes(context -> {
                            String status = JumpManager.isJumpEnabled() ? "§aвключен" : "§cотключен";
                            context.getSource().sendSuccess(
                                    new StringTextComponent("Прыжок " + status),
                                    true
                            );
                            return 1;
                        })
        );
    }
}
