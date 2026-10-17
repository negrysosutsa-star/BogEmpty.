package com.fox.bogempty.mixin;

import com.fox.bogempty.BogEmpty;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.module.render.NoRender;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    
    // Отключаем тряску камеры (Hurt Cam)
    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void onBobView(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        for (Module m : BogEmpty.modules) {
            if (m instanceof NoRender nr && nr.isHurtCam()) {
                ci.cancel();
            }
        }
    }

    // Отключаем огонь на экране (Fire Overlay)
    @Inject(method = "renderFloatingItem", at = @At("HEAD"), cancellable = true)
    private void onRenderFloatingItem(CallbackInfo ci) {
        // В 1.21.4 огонь рендерится в рендерере худа, но некоторые версии используют GameRenderer
        // Если не сработает, мы отключим его через HudRenderCallback
    }
}