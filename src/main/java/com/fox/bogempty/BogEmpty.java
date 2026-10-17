package com.fox.bogempty;

import com.fox.bogempty.config.Config;
import com.fox.bogempty.gui.ClickGUI;
import com.fox.bogempty.module.combat.AimAssist;
import com.fox.bogempty.module.combat.TriggerBot;
import com.fox.bogempty.module.render.ESPModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class BogEmpty implements ClientModInitializer {
    public static TriggerBot triggerBot;
    public static AimAssist aimAssist;
    public static ESPModule espModule;
    
    private static boolean guiKeyWasDown = false;

    @Override
    public void onInitializeClient() {
        Config.load();
        triggerBot = new TriggerBot();
        aimAssist = new AimAssist();
        espModule = new ESPModule();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            // Скрываем хотбар и руку при открытом меню
            mc.options.hudHidden = mc.currentScreen instanceof ClickGUI;
            
            long handle = mc.getWindow().getHandle();
            boolean down = GLFW.glfwGetKey(handle, Config.bindGui) == GLFW.GLFW_PRESS;
            
            // Открытие / закрытие меню
            if (down && !guiKeyWasDown) {
                if (mc.currentScreen == null) {
                    mc.setScreen(new ClickGUI());
                } else if (mc.currentScreen instanceof ClickGUI) {
                    mc.setScreen(null);
                }
            }
            guiKeyWasDown = down;

            // Тик триггера
            triggerBot.onTick(mc);
        });

        // Рендер аима (плавность)
        WorldRenderEvents.START.register(ctx -> aimAssist.onFrame(MinecraftClient.getInstance()));
        
        // Рендер ESP
        WorldRenderEvents.LAST.register(ctx -> espModule.onRender(ctx.matrixStack(), ctx.camera(), ctx.tickCounter().getTickDelta(true)));

        // Рендер вотермарки
        HudRenderCallback.EVENT.register((ctx, tickDelta) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || !Config.watermarkEnabled || mc.currentScreen instanceof ClickGUI) return;
            
            String text = "BogEmpty";
            int width = mc.textRenderer.getWidth(text);
            
            // Фон
            ctx.fill(5, 5, 15 + width, 20, 0xFF161616);
            // Полоска слева
            ctx.fill(5, 5, 7, 20, 0xFF00E5FF);
            // Текст
            ctx.drawText(mc.textRenderer, text, 10, 9, 0xFF00E5FF, true);
        });
    }
}