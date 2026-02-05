package org.mozilla.HuntEngine.Screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.mozilla.HuntEngine.HuntEngine;
import org.mozilla.HuntEngine.Config.BlockEventConfigManager;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockManagerScreen extends Screen {
    private BlockPos blockPos;
    private String blockName;
    private List<String> eventTypes;
    private int selectedEventIndex = 0;
    private TextFieldWidget scriptPathField;
    private Map<String, String> blockEventScripts;
    private ServerPlayerEntity player;
    private BlockEventConfigManager configManager;
    
    public BlockManagerScreen(String blockName, BlockPos blockPos, ServerPlayerEntity player) {
        super(new StringTextComponent("Block Manager"));
        this.blockName = blockName;
        this.blockPos = blockPos;
        this.player = player;
        this.eventTypes = new ArrayList<>();
        this.blockEventScripts = new HashMap<>();
        this.configManager = new BlockEventConfigManager(player);
        initializeEventTypes();
        loadBlockConfig();
    }

    private void initializeEventTypes() {
        eventTypes.add("onRightClick");
        eventTypes.add("onCollide");
        eventTypes.add("onDestroy");
        eventTypes.add("onPlaced");
    }

    private void loadBlockConfig() {
        for (String event : eventTypes) {
            String script = configManager.getEventScript(blockPos, event);
            if (script != null) {
                blockEventScripts.put(event, script);
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        
        // Кнопки выбора события
        int buttonY = 35;
        for (int i = 0; i < eventTypes.size(); i++) {
            final int index = i;
            String eventName = eventTypes.get(i);
            int x = 10 + (i % 2) * 160;
            int y = buttonY + (i / 2) * 25;
            
            this.addButton(new Button(x, y, 150, 20,
                new StringTextComponent(eventName + (selectedEventIndex == i ? " ✓" : "")),
                (button) -> {
                    selectedEventIndex = index;
                }));
        }
        
        // Поле для ввода пути к JS файлу
        scriptPathField = new TextFieldWidget(this.font, 10, 110, 300, 20,
            new StringTextComponent("Путь к JS файлу"));
        scriptPathField.setValue(blockEventScripts.getOrDefault(eventTypes.get(selectedEventIndex), ""));
        this.children.add(scriptPathField);
        this.setFocused(scriptPathField);
        
        // Кнопка сохранить и выполнить
        this.addButton(new Button(10, 140, 150, 20,
            new StringTextComponent("Сохранить"),
            (button) -> {
                String scriptPath = scriptPathField.getValue();
                String eventType = eventTypes.get(selectedEventIndex);
                
                if (!scriptPath.isEmpty()) {
                    blockEventScripts.put(eventType, scriptPath);
                    
                    // Сохранить конфиг по координатам блока
                    configManager.setEventScript(blockPos, eventType, scriptPath);
                    
                    player.sendMessage(
                        new StringTextComponent("§aСкрипт для события " + eventType + " успешно привязан!"),
                        player.getUUID()
                    );
                } else {
                    player.sendMessage(
                        new StringTextComponent("§cУкажите путь к скрипту!"),
                        player.getUUID()
                    );
                }
            }));
        
        // Кнопка закрытия
        this.addButton(new Button(170, 140, 140, 20,
            new StringTextComponent("Закрыть"),
            (button) -> {
                this.onClose();
            }));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        
        this.drawString(matrixStack, this.font, "Блок: " + blockName + " [" + blockPos.getX() + ", " + blockPos.getY() + ", " + blockPos.getZ() + "]", 10, 10, 0xFFFFFF);
        this.drawString(matrixStack, this.font, "Выберите событие:", 10, 25, 0xFFFF00);
        this.drawString(matrixStack, this.font, "Текущее событие: " + eventTypes.get(selectedEventIndex), 10, 90, 0x00FF00);
        
        scriptPathField.render(matrixStack, mouseX, mouseY, partialTicks);
        
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
