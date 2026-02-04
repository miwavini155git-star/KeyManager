package org.mozilla.HuntEngine.Command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.storage.FolderName;
import org.mozilla.HuntEngine.HuntEngine;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class HuntCommand {
    
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            Commands.literal("hunt")
                .then(
                    Commands.literal("reload")
                        .then(
                            Commands.argument("file", StringArgumentType.greedyString())
                                .suggests(getFileSuggestions())
                                .executes(context -> {
                                    try {
                                        return reload(context);
                                    } catch (Exception e) {
                                        return 0;
                                    }
                                })
                        )
                        .executes(context -> {
                            try {
                                return reloadAll(context);
                            } catch (Exception e) {
                                return 0;
                            }
                        })
                )
        );
    }
    
    private static SuggestionProvider<CommandSource> getFileSuggestions() {
        return (context, builder) -> {
            try {
                CommandSource source = context.getSource();
                if (source.hasPermission(2)) {
                    ServerPlayerEntity player = source.getPlayerOrException();
                    Path huntEngineDir = getHuntEngineDirectory(player);
                    
                    if (Files.exists(huntEngineDir)) {
                        List<String> suggestions = new ArrayList<>();
                        collectScriptPaths(huntEngineDir, huntEngineDir, suggestions);
                        
                        for (String suggestion : suggestions) {
                            builder.suggest(suggestion);
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
            return builder.buildFuture();
        };
    }
    
    private static void collectScriptPaths(Path baseDir, Path currentDir, List<String> suggestions) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentDir)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    collectScriptPaths(baseDir, path, suggestions);
                } else if (path.toString().endsWith(".js")) {
                    String relativePath = baseDir.relativize(path).toString().replace("\\", "/");
                    suggestions.add(relativePath);
                }
            }
        }
    }
    
    private static int reload(CommandContext<CommandSource> context) throws Exception {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        String fileName = StringArgumentType.getString(context, "file");
        
        Path huntEngineDir = getHuntEngineDirectory(player);
        Path scriptPath = huntEngineDir.resolve(fileName);
        
        if (!Files.exists(scriptPath)) {
            player.sendMessage(new StringTextComponent("§cScript file not found: " + fileName), player.getUUID());
            return 0;
        }
        
        if (!scriptPath.toString().endsWith(".js")) {
            player.sendMessage(new StringTextComponent("§cOnly .js files are allowed"), player.getUUID());
            return 0;
        }
        
        try {
            // Запланировать выполнение скрипта через 1 секунду (20 тиков)
            player.getServer().execute(() -> {
                try {
                    Thread.sleep(500); // 1 секунда = 1000 миллисекунд
                    HuntEngine engine = new HuntEngine(player);
                    engine.executeScript(scriptPath);
                    player.sendMessage(new StringTextComponent("§aScript reloaded: " + fileName), player.getUUID());
                } catch (Exception e) {
                    player.sendMessage(new StringTextComponent("§cError: " + e.getMessage()), player.getUUID());
                }
            });
            return 1;
        } catch (Exception e) {
            player.sendMessage(new StringTextComponent("§cError: " + e.getMessage()), player.getUUID());
            return 0;
        }
    }
    
    private static int reloadAll(CommandContext<CommandSource> context) throws Exception {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        
        Path huntEngineDir = getHuntEngineDirectory(player);
        
        if (!Files.exists(huntEngineDir)) {
            player.sendMessage(new StringTextComponent("§cHuntEngine directory not found"), player.getUUID());
            return 0;
        }
        
        try {
            // Запланировать выполнение скриптов через 1 секунду (20 тиков)
            player.getServer().execute(() -> {
                try {
                    Thread.sleep(500); // 1 секунда = 1000 миллисекунд
                    HuntEngine engine = new HuntEngine(player);
                    engine.executeScriptsFromDirectory(huntEngineDir);
                    player.sendMessage(new StringTextComponent("§aAll scripts reloaded"), player.getUUID());
                } catch (Exception e) {
                    player.sendMessage(new StringTextComponent("§cError: " + e.getMessage()), player.getUUID());
                }
            });
            return 1;
        } catch (Exception e) {
            player.sendMessage(new StringTextComponent("§cError: " + e.getMessage()), player.getUUID());
            return 0;
        }
    }
    
    private static Path getHuntEngineDirectory(ServerPlayerEntity player) {
        return player.getServer().getWorldPath(FolderName.ROOT).resolve("HuntEngine");
    }
}
