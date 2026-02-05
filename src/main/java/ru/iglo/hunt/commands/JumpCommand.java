package ru.iglo.hunt.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import ru.iglo.hunt.managers.JumpManager;

public class JumpCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("jump")
                        .requires(source -> source.hasPermission(2)) // Только для операторов
                        .then(Commands.literal("on")
                                .executes(context -> {
                                    JumpManager.setJumpEnabled(true);
                                    context.getSource().sendSuccess(
                                            new StringTextComponent("§aПрыжок включен"),
                                            true
                                    );
                                    return 1;
                                }))
                        .then(Commands.literal("off")
                                .executes(context -> {
                                    JumpManager.setJumpEnabled(false);
                                    context.getSource().sendSuccess(
                                            new StringTextComponent("§cПрыжок отключен"),
                                            true
                                    );
                                    return 1;
                                }))
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
