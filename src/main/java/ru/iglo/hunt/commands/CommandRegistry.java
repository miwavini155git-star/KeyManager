package ru.iglo.hunt.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import ru.iglo.hunt.Hunt;

@Mod.EventBusSubscriber(modid = Hunt.MODID)
public class CommandRegistry {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();

        // Регистрируем все команды
        HospitalKeyCommands.register(dispatcher);
        JumpCommand.register(dispatcher);
        // Добавьте здесь другие команды по мере создания

        System.out.println("[Hunt] Commands registered successfully");
    }
}