package ru.iglo.hunt.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.iglo.hunt.Hunt;

import java.io.IOException;
import java.io.InputStream;

@Mod.EventBusSubscriber(modid = Hunt.MODID, value = Dist.CLIENT)
public class SimpleWindowManager {

    private static boolean iconInitialized = false;
    private static long tickCounter = 0;
    private static final String BASE_TITLE = "Hunt";

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() == null) return;

        try {
            // Увеличиваем счетчик тиков
            tickCounter++;

            // Создаем динамический заголовок на основе счетчика тиков
            String dynamicTitle = createDynamicTitle();

            // Устанавливаем новый заголовок каждый тик
            mc.getWindow().setTitle(dynamicTitle);

            // Иконку устанавливаем только один раз
            if (!iconInitialized) {
                setWindowIcon(mc);
                iconInitialized = true;
                Hunt.LOGGER.info("Window icon set successfully");
            }

            // Логируем каждые 100 тиков для отладки
            if (tickCounter % 100 == 0) {
                Hunt.LOGGER.debug("Window title updated: {}", dynamicTitle);
            }
        } catch (Exception e) {
            if (!iconInitialized) {
                Hunt.LOGGER.error("Failed to set window icon", e);
                iconInitialized = true;
            }
        }
    }

    private static String createDynamicTitle() {
        // Примеры разных вариантов динамического заголовка:

        // 1. Просто счетчик тиков
        // return BASE_TITLE + " - Tick: " + tickCounter;

        // 2. Циклический счетчик
        // long cycle = tickCounter % 1000;
        // return BASE_TITLE + " [" + cycle + "/1000]";

        // 3. Вращающийся индикатор
        String[] spinner = {"|", "/", "-", "\\"};
        String spin = spinner[(int)(tickCounter % 4)];
        return BASE_TITLE + " " + spin + " [" + (tickCounter % 1000) + "]";

        // 4. Время игры
        // Minecraft mc = Minecraft.getInstance();
        // if (mc.player != null && mc.level != null) {
        //     long gameTime = mc.level.getGameTime() % 24000;
        //     long hours = (gameTime / 1000 + 6) % 24;
        //     long minutes = (gameTime % 1000) * 60 / 1000;
        //     return String.format("%s %02d:%02d", BASE_TITLE, hours, minutes);
        // }
        // return BASE_TITLE;

        // 5. FPS
        // Minecraft mc = Minecraft.getInstance();
        // int fps = Minecraft.getInstance().fps;
        // return BASE_TITLE + " | FPS: " + fps;
    }

    private static void setWindowIcon(Minecraft mc) throws IOException {
        try (InputStream smallIconStream = loadIconStream(mc, new ResourceLocation(Hunt.MODID, "textures/icon/icon_16x16.png"));
             InputStream mediumIconStream = loadIconStream(mc, new ResourceLocation(Hunt.MODID, "textures/icon/icon_32x32.png"))) {

            mc.getWindow().setIcon(smallIconStream, mediumIconStream);
        }
    }

    private static InputStream loadIconStream(Minecraft mc, ResourceLocation location) throws IOException {
        IResource resource = mc.getResourceManager().getResource(location);
        return resource.getInputStream();
    }
}
