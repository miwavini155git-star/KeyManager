package ru.iglo.hunt.mixin;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.iglo.hunt.managers.JumpManager;

@Mixin(PlayerEntity.class)
public class JumpPlayerMixin {

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void onJump(CallbackInfo ci) {
        // Отменяем прыжок если он отключен
        if (!JumpManager.isJumpEnabled()) {
            ci.cancel();
        }
    }
}