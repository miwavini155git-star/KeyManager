package org.mozilla.HuntEngine.Config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.mozilla.HuntEngine.HuntEngine;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class BlockEventConfigManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path configDir;
    private Map<String, BlockEventConfig> configs;

    public BlockEventConfigManager(ServerPlayerEntity player) {
        HuntEngine engine = new HuntEngine(player);
        this.configDir = engine.getHuntEngineFolder().resolve("configs");
        this.configs = new HashMap<>();
        loadAllConfigs();
    }

    private void loadAllConfigs() {
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
                return; // Если папка только что создана - конфигов нет
            }

            File[] files = configDir.toFile().listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null && files.length > 0) {
                for (File file : files) {
                    try (FileReader reader = new FileReader(file)) {
                        BlockEventConfig config = gson.fromJson(reader, BlockEventConfig.class);
                        if (config != null) {
                            configs.put(config.getKey(), config);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getKey(BlockPos pos) {
        return pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
    }

    public BlockEventConfig getBlockConfig(BlockPos pos) {
        String key = getKey(pos);
        return configs.computeIfAbsent(key, id -> new BlockEventConfig(pos.getX(), pos.getY(), pos.getZ()));
    }

    public void saveBlockConfig(BlockPos pos) {
        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            String key = getKey(pos);
            BlockEventConfig config = configs.get(key);
            if (config != null) {
                String filename = key + ".json";
                Path configPath = configDir.resolve(filename);

                try (FileWriter writer = new FileWriter(configPath.toFile())) {
                    gson.toJson(config, writer);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setEventScript(BlockPos pos, String eventType, String scriptPath) {
        BlockEventConfig config = getBlockConfig(pos);
        config.setEventScript(eventType, scriptPath);
        saveBlockConfig(pos);
    }

    public String getEventScript(BlockPos pos, String eventType) {
        String key = getKey(pos);
        BlockEventConfig config = configs.get(key);
        if (config != null) {
            return config.getEventScript(eventType);
        }
        return null;
    }

    public boolean hasEventScript(BlockPos pos, String eventType) {
        String key = getKey(pos);
        BlockEventConfig config = configs.get(key);
        if (config != null) {
            return config.hasEventScript(eventType);
        }
        return false;
    }
}
