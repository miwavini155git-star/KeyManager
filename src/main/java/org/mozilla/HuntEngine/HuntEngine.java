package org.mozilla.HuntEngine;

import org.mozilla.HuntEngine.JS.WorldJS;
import org.mozilla.javascript.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.DirectoryStream;
import java.util.ArrayList;
import java.util.List;

public class HuntEngine {
    private final ServerPlayerEntity player;
    
    public HuntEngine(ServerPlayerEntity player) {
        this.player = player;
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
            
            // Используем PlayerJS для создания объекта Player
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
}
