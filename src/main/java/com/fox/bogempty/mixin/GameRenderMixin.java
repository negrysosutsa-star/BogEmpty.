package com.fox.bogempty.mixin;

import com.fox.bogempty.BogEmpty;
import com.fox.bogempty.module.Module;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    
    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void onBobView(CallbackInfo ci) {
        // Отменяем тряску камеры, если модуль включен
        for (Module m : BogEmpty.modules) {
            if (m.getName().equals("NoHurtCam") && m.isEnabled()) {
                ci.cancel();
            }
        }
    }
}