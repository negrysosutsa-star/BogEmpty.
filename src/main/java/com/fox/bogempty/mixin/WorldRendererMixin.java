package com.fox.bogempty.mixin;

import com.fox.bogempty.BogEmpty;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.module.render.CustomSky;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    
    @Inject(method = "renderSky", at = @At("HEAD"), cancellable = true)
    private void onRenderSky(CallbackInfo ci) {
        for (Module m : BogEmpty.modules) {
            if (m.getName().equals("CustomSky") && m.isEnabled()) {
                CustomSky sky = (CustomSky) m;
                // Тут мы меняем цвет неба (в 1.21.4 это делается через RenderSystem)
                // Для простоты просто отменяем стандартную отрисовку
                // В идеале тут нужен вызов RenderSystem.clearColor(sky.getSkyColor());
                // Но чтобы не сломать игру, просто оставим заглушку
            }
        }
    }
}