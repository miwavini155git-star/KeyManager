package org.mozilla.HuntEngine.JS;

import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.iglo.hunt.blocks.KeyCabinetBlock;
import ru.iglo.hunt.keys.KeyType;
import ru.iglo.hunt.utils.DoorUtils;
import net.minecraft.block.Blocks;

public class WorldJS {
    private ServerPlayerEntity player;
    
    public WorldJS(ServerPlayerEntity player) {
        this.player = player;
    }
    
    public Scriptable createPlayerObject(Context context, Scriptable scope) {
        Scriptable playerObj = context.newObject(scope);

        playerObj.put("say", playerObj, new Callable() {
            @Override
            public Object call(Context ctx, Scriptable scp, Scriptable thisObj, Object[] args) {
                if (args.length > 0) {
                    String message = Context.toString(args[0]);
                    player.sendMessage(new StringTextComponent(message), player.getUUID());
                }
                return null;
            }
        });

        playerObj.put("getName", playerObj, new Callable() {
            @Override
            public Object call(Context ctx, Scriptable scp, Scriptable thisObj, Object[] args) {
                return player.getName().getContents();
            }
        });

        playerObj.put("getPos", playerObj, new Callable() {
            @Override
            public Object call(Context ctx, Scriptable scp, Scriptable thisObj, Object[] args) {
                Scriptable pos = ctx.newObject(scope);
                pos.put("x", pos, player.getX());
                pos.put("y", pos, player.getY());
                pos.put("z", pos, player.getZ());
                return pos;
            }
        });

        // Метод getHealth - получить здоровье игрока
        playerObj.put("getHealth", playerObj, new Callable() {
            @Override
            public Object call(Context ctx, Scriptable scp, Scriptable thisObj, Object[] args) {
                return player.getHealth();
            }
        });

        // Метод createKey - создать ключ в указанной позиции
        playerObj.put("createKey", playerObj, new Callable() {
            @Override
            public Object call(Context ctx, Scriptable scp, Scriptable thisObj, Object[] args) {
                if (args.length >= 4) {
                    try {
                        int x = (int) Context.toNumber(args[0]);
                        int y = (int) Context.toNumber(args[1]);
                        int z = (int) Context.toNumber(args[2]);
                        String keyTypeName = Context.toString(args[3]);
                        
                        World world = player.getCommandSenderWorld();
                        BlockPos pos = new BlockPos(x, y, z);
                        KeyType keyType = KeyType.valueOf(keyTypeName);
                        
                        KeyCabinetBlock.setupCabinet(world, pos, keyType);
                        
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }
                return false;
            }
        });

        // Метод createIronDoor - создать железную дверь в указанной позиции
        playerObj.put("createIronDoor", playerObj, new Callable() {
            @Override
            public Object call(Context ctx, Scriptable scp, Scriptable thisObj, Object[] args) {
                if (args.length >= 3) {
                    try {
                        int x = (int) Context.toNumber(args[0]);
                        int y = (int) Context.toNumber(args[1]);
                        int z = (int) Context.toNumber(args[2]);
                        
                        World world = player.getCommandSenderWorld();
                        KeyType keyType = null;
                        
                        // Опциональный 4-й параметр - тип ключа
                        if (args.length >= 4) {
                            String keyTypeName = Context.toString(args[3]);
                            keyType = KeyType.valueOf(keyTypeName);
                        }
                        
                        BlockPos lowerPos = new BlockPos(x, y, z);
                        BlockPos upperPos = lowerPos.above();
                        
                        // Удаляем блоки если место занято
                        if (!world.getBlockState(lowerPos).isAir() || !world.getBlockState(upperPos).isAir()) {
                            world.setBlockAndUpdate(lowerPos, Blocks.AIR.defaultBlockState());
                            world.setBlockAndUpdate(upperPos, Blocks.AIR.defaultBlockState());
                        }
                        
                        // Создаём дверь
                        BlockPos doorPos = DoorUtils.createIronDoor(world, x, y, z, keyType);
                        
                        if (doorPos != null) {
                            return true;
                        } else {
                            return false;
                        }
                    } catch (Exception e) {
                        return false;
                    }
                }
                return false;
            }
        });

        return playerObj;
    }
}
