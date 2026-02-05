package org.mozilla.HuntEngine;

import net.minecraft.client.Minecraft;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.mozilla.HuntEngine.JS.WorldJS;
import org.mozilla.HuntEngine.Screen.BlockManagerScreen;
import org.mozilla.HuntEngine.Config.BlockEventConfigManager;
import org.mozilla.javascript.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import ru.iglo.hunt.Hunt;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.DirectoryStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class HuntEngine {
    private final ServerPlayerEntity player;

    public HuntEngine(ServerPlayerEntity player) {
        this.player = player;
    }

    public Path getHuntEngineFolder() {
        return player.getServer().getWorldPath(FolderName.ROOT).resolve("HuntEngine");
    }

    public void executeScript(Path scriptPath) {
        if (!Files.exists(scriptPath)) {
            player.sendMessage(new StringTextComponent("§cScript file not found: " + scriptPath), player.getUUID());
            return;
        }

        try {
            String scriptContent = new String(Files.readAllBytes(scriptPath));
            executeScriptContent(scriptContent);
        } catch (IOException e) {
            player.sendMessage(new StringTextComponent("§cError reading script: " + e.getMessage()), player.getUUID());
        }
    }

    public void executeScriptsFromDirectory(Path directory) {
        if (!Files.isDirectory(directory)) {
            player.sendMessage(new StringTextComponent("§cDirectory not found: " + directory), player.getUUID());
            return;
        }

        try {
            List<Path> scripts = new ArrayList<>();
            collectScriptsRecursively(directory, scripts);

            for (Path scriptPath : scripts) {
                executeScript(scriptPath);
            }
        } catch (IOException e) {
            player.sendMessage(new StringTextComponent("§cError reading scripts: " + e.getMessage()), player.getUUID());
        }
    }

    private void collectScriptsRecursively(Path directory, List<Path> scripts) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                if (Files.isDirectory(path)) {
                    collectScriptsRecursively(path, scripts);
                } else if (path.toString().endsWith(".js")) {
                    scripts.add(path);
                }
            }
        }
    }

    public void executeScriptContent(String script) {
        try {
            Context context = Context.enter();
            Scriptable scope = context.initStandardObjects();

            WorldJS playerJS = new WorldJS(player);
            Scriptable playerObj = playerJS.createPlayerObject(context, scope);

            scope.put("scene", scope, playerObj);

            context.evaluateString(scope, script, "<script>", 1, null);
        } catch (Exception e) {
            player.sendMessage(new StringTextComponent("§cScript error: " + e.getMessage()), player.getUUID());
        } finally {
            Context.exit();
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class EventHandler {
        @SubscribeEvent
        public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
            if (event.getWorld().isClientSide) {
                return;
            }

            if (event.isCanceled()) {
                return;
            }

            // Only handle main-hand interactions to avoid duplicate calls
            if (event.getHand() != net.minecraft.util.Hand.MAIN_HAND) {
                return;
            }

            net.minecraft.entity.player.PlayerEntity player = event.getPlayer();

            BlockPos blockPos = event.getPos();
            Block block = event.getWorld().getBlockState(blockPos).getBlock();
            String blockId = block.getRegistryName().toString();

            if (player instanceof ServerPlayerEntity) {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
                boolean isShiftPressed = net.minecraft.client.Minecraft.getInstance().player.isShiftKeyDown();

                // Log debug to console only (do not spam player chat)
                Hunt.LOGGER.debug("[DEBUG] Клик на блоке [" + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ() + "] Shift=" + isShiftPressed);

                if (isShiftPressed && player.getMainHandItem().isEmpty()) {
                    // Shift + правый клик - открыть экран настройки только для креатива
                    if (player.isCreative()) {
                        Minecraft.getInstance().setScreen(new BlockManagerScreen(blockId, blockPos, serverPlayer));
                    } else {
                        // Non-creative players cannot open the config screen with Shift
                    }
                } else if (!isShiftPressed) {
                    // Обычный правый клик - выполнить привязанный скрипт для этого блока (все режимы)
                    executeBlockEvent(serverPlayer, blockPos, "onRightClick");
                }
            }
        }

        @SubscribeEvent
        public static void onBlockBreak(BlockEvent.BreakEvent event) {
            if (!(event.getWorld() instanceof ServerWorld)) {
                return;
            }

            net.minecraft.entity.player.PlayerEntity player = event.getPlayer();
            BlockPos blockPos = event.getPos();
            Block block = event.getWorld().getBlockState(blockPos).getBlock();
            String blockId = block.getRegistryName().toString();

            if (player instanceof ServerPlayerEntity) {
                ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
                // Log debug to console only (do not spam player chat)
                Hunt.LOGGER.debug("[DEBUG] Разрушение блока [" + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ() + "]");
                // Выполнить скрипт если он привязан к событию onDestroy для этого блока
                executeBlockEvent(serverPlayer, blockPos, "onDestroy");
            }
        }

        private static void executeBlockEvent(ServerPlayerEntity player, BlockPos blockPos, String eventType) {
            try {
                BlockEventConfigManager configManager = new BlockEventConfigManager(player);
                
                if (configManager.hasEventScript(blockPos, eventType)) {
                    String scriptPath = configManager.getEventScript(blockPos, eventType);
                    // Log script execution to console only
                    Hunt.LOGGER.info("[HuntEngine] Выполнение скрипта для события: " + eventType + " | " + scriptPath);

                    HuntEngine engine = new HuntEngine(player);
                    
                    java.nio.file.Path fullPath;
                    if (scriptPath.contains("/") || scriptPath.contains("\\")) {
                        fullPath = Paths.get(scriptPath);
                    } else {
                        fullPath = engine.getHuntEngineFolder().resolve(scriptPath);
                    }
                    
                    engine.executeScript(fullPath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
