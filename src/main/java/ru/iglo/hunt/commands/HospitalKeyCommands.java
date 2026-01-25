package ru.iglo.hunt.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import ru.iglo.hunt.blocks.KeyCabinetBlock;
import ru.iglo.hunt.items.HospitalKeyItem;
import ru.iglo.hunt.keys.KeyType;
import ru.iglo.hunt.managers.HospitalKeyManager;

import java.util.Locale;

public class HospitalKeyCommands {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("hospitalkey")
                        .requires(source -> source.hasPermission(2)) // Только для операторов

                        // Команда: /hospitalkey give <type>
                        .then(Commands.literal("give")
                                .then(Commands.argument("type", StringArgumentType.string())
                                        .suggests((context, builder) -> {
                                            // Автодополнение для типов ключей
                                            String[] keyTypes = {
                                                    "cabinet_1", "cabinet_2", "cabinet_3",
                                                    "library", "security", "server",
                                                    "basement", "morgue", "chief",
                                                    "electrical", "master"
                                            };
                                            for (String type : keyTypes) {
                                                builder.suggest(type);
                                            }
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            String type = StringArgumentType.getString(context, "type").toLowerCase(Locale.ROOT);
                                            CommandSource source = context.getSource();

                                            // Маппинг ввода пользователя на KeyType
                                            KeyType keyType;
                                            switch (type) {
                                                case "cabinet_1": keyType = KeyType.CABINET_1; break;
                                                case "cabinet_2": keyType = KeyType.CABINET_2; break;
                                                case "cabinet_3": keyType = KeyType.CABINET_3; break;
                                                case "library": keyType = KeyType.LIBRARY; break;
                                                case "security": keyType = KeyType.SECURITY; break;
                                                case "server": keyType = KeyType.SERVER_ROOM; break;
                                                case "basement": keyType = KeyType.BASEMENT; break;
                                                case "morgue": keyType = KeyType.MORGUE; break;
                                                case "chief": keyType = KeyType.CHIEF_DOCTOR; break;
                                                case "electrical": keyType = KeyType.ELECTRICAL; break;
                                                case "master": keyType = KeyType.MASTER_KEY; break;
                                                default:
                                                    source.sendFailure(new StringTextComponent("Неизвестный тип ключа. Доступные: cabinet_1, cabinet_2, cabinet_3, library, security, server, basement, morgue, chief, electrical, master"));
                                                    return 0;
                                            }

                                            ItemStack key = HospitalKeyItem.createKey(keyType);

                                            if (source.getPlayerOrException().addItem(key)) {
                                                source.sendSuccess(
                                                        new StringTextComponent("§aВыдан ключ: " + keyType.getDisplayName()),
                                                        true
                                                );
                                                return 1;
                                            } else {
                                                source.sendFailure(new StringTextComponent("§cНе удалось выдать ключ (инвентарь полон?)"));
                                                return 0;
                                            }
                                        })
                                )
                        )

                        // Команда: /hospitalkey giveall
                        .then(Commands.literal("giveall")
                                .executes(context -> {
                                    CommandSource source = context.getSource();
                                    int count = 0;

                                    for (KeyType type : KeyType.values()) {
                                        ItemStack key = HospitalKeyItem.createKey(type);
                                        if (source.getPlayerOrException().addItem(key)) {
                                            count++;
                                        }
                                    }

                                    source.sendSuccess(
                                            new StringTextComponent("§aВыдано " + count + " ключей из " + KeyType.values().length),
                                            true
                                    );
                                    return 1;
                                })
                        )

                        // Команда: /hospitalkey setup
                        .then(Commands.literal("setup")
                                .executes(context -> {
                                    CommandSource source = context.getSource();

                                    try {
                                        HospitalKeyManager.setupHospitalKeySystem();
                                        source.sendSuccess(
                                                new StringTextComponent("§aСистема ключей больницы установлена!"),
                                                true
                                        );
                                        return 1;
                                    } catch (Exception e) {
                                        source.sendFailure(new StringTextComponent("§cОшибка при установке системы ключей: " + e.getMessage()));
                                        return 0;
                                    }
                                })
                        )

                        // Команда: /hospitalkey admin
                        .then(Commands.literal("admin")
                                .executes(context -> {
                                    CommandSource source = context.getSource();
                                    ItemStack adminKey = HospitalKeyManager.createAdminKeySet();

                                    if (source.getPlayerOrException().addItem(adminKey)) {
                                        source.sendSuccess(
                                                new StringTextComponent("§6Выдан ключ суперадминистратора!"),
                                                true
                                        );
                                        return 1;
                                    } else {
                                        source.sendFailure(new StringTextComponent("§cНе удалось выдать ключ"));
                                        return 0;
                                    }
                                })
                        )
// В HospitalKeyCommands.java добавьте:
                        .then(Commands.literal("spawncabinet")
                                .executes(context -> {
                                    CommandSource source = context.getSource();
                                    World world = source.getLevel();
                                    BlockPos pos = new BlockPos(0, 4, 0);

                                    KeyCabinetBlock.setupCabinet(world, pos, KeyType.MORGUE);

                                    source.sendSuccess(
                                            new StringTextComponent("§aБлок с ключом установлен на (0, 4, 0)"),
                                            true
                                    );
                                    return 1;
                                })
                        )
                        // Команда: /hospitalkey random
                        .then(Commands.literal("random")
                                .executes(context -> {
                                    CommandSource source = context.getSource();
                                    ItemStack randomKey = HospitalKeyManager.createRandomHospitalKey();

                                    if (source.getPlayerOrException().addItem(randomKey)) {
                                        String keyName = randomKey.getDisplayName().getString();
                                        source.sendSuccess(
                                                new StringTextComponent("§aВыдан случайный ключ: " + keyName),
                                                true
                                        );
                                        return 1;
                                    } else {
                                        source.sendFailure(new StringTextComponent("§cНе удалось выдать ключ"));
                                        return 0;
                                    }
                                })
                        )

                        // Команда: /hospitalkey test
                        .then(Commands.literal("test")
                                .executes(context -> {
                                    CommandSource source = context.getSource();
                                    ItemStack itemInHand = source.getPlayerOrException().getMainHandItem();

                                    if (itemInHand.isEmpty()) {
                                        source.sendSuccess(new StringTextComponent("§cВ руке ничего нет"), false);
                                        return 0;
                                    }

                                    source.sendSuccess(new StringTextComponent("§aПредмет: " + itemInHand.getDisplayName().getString()), false);
                                    source.sendSuccess(new StringTextComponent("§aКоличество: " + itemInHand.getCount()), false);
                                    source.sendSuccess(new StringTextComponent("§aNBT: " + itemInHand.getTag()), false);

                                    return 1;
                                })
                        )

                        // Команда: /hospitalkey help
                        .then(Commands.literal("help")
                                .executes(context -> {
                                    CommandSource source = context.getSource();

                                    source.sendSuccess(new StringTextComponent("§6=== Hospital Key Commands ==="), false);
                                    source.sendSuccess(new StringTextComponent("§e/hospitalkey give <type> §7- Выдать ключ определенного типа"), false);
                                    source.sendSuccess(new StringTextComponent("§e/hospitalkey giveall §7- Выдать все ключи"), false);
                                    source.sendSuccess(new StringTextComponent("§e/hospitalkey setup §7- Установить систему ключей в мире"), false);
                                    source.sendSuccess(new StringTextComponent("§e/hospitalkey admin §7- Получить ключ админа"), false);
                                    source.sendSuccess(new StringTextComponent("§e/hospitalkey random §7- Получить случайный ключ"), false);
                                    source.sendSuccess(new StringTextComponent("§e/hospitalkey help §7- Показать эту справку"), false);
                                    source.sendSuccess(new StringTextComponent("§7Доступные типы: cabinet_1, cabinet_2, cabinet_3, library, security, server, basement, morgue, chief, electrical, master"), false);

                                    return 1;
                                })
                        )
        );
    }
}