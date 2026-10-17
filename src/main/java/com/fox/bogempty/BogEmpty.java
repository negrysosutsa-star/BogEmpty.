package com.fox.bogempty;

import com.fox.bogempty.config.Config;
import com.fox.bogempty.gui.ClickGUI;
import com.fox.bogempty.module.combat.AimAssist;
import com.fox.bogempty.module.combat.TriggerBot;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public class BogEmpty implements ClientModInitializer {
    public static TriggerBot triggerBot;
    public static AimAssist aimAssist;
    
    private static boolean bindWasPressed = false;
    private static boolean aimBindWasPressed = false;
    private static boolean guiBindWasPressed = false;

    @Override
    public void onInitializeClient() {
        Config.load();
        triggerBot = new TriggerBot();
        aimAssist = new AimAssist();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            handleBinds(mc);
            triggerBot.onTick(mc);
        });

        WorldRenderEvents.START.register(ctx -> aimAssist.onFrame(MinecraftClient.getInstance()));

        // РЕНДЕР ВОТЕРМАРКИ (HUD)
        HudRenderCallback.EVENT.register((ctx, tickDelta) -> {
            if (!Config.watermarkEnabled || mc.currentScreen instanceof ClickGUI) return;
            
            // Красивая фиолетовая вотермарка
            String text = "BogEmpty";
            int width = mc.textRenderer.getWidth(text);
            
            // Фон
            ctx.fill(5, 5, 15 + width, 20, 0xFF161616);
            // Полоска слева
            ctx.fill(5, 5, 7, 20, 0xFF9D4EDD);
            // Текст
            ctx.drawText(mc.textRenderer, text, 10, 9, 0xFFC77DFF, true);
        });
    }

    private void handleBinds(MinecraftClient mc) {
        long handle = mc.getWindow().getHandle();
        
        // Открытие меню (Правый Shift по умолчанию)
        if (Config.bindGui != -1) {
            boolean down = GLFW.glfwGetKey(handle, Config.bindGui) == GLFW.GLFW_PRESS;
            if (down && !guiBindWasPressed) {
                if (mc.currentScreen == null) mc.setScreen(new ClickGUI());
                else if (mc.currentScreen instanceof ClickGUI) mc.setScreen(null);
            }
            guiBindWasPressed = down;
        }

        if (mc.currentScreen != null) return;

        if (Config.bindTrigger != -1) {
            boolean down = GLFW.glfwGetKey(handle, Config.bindTrigger) == GLFW.GLFW_PRESS;
            if (down && !bindWasPressed) Config.triggerEnabled = !Config.triggerEnabled;
            bindWasPressed = down;
        }

        if (Config.bindAim != -1) {
            boolean down = GLFW.glfwGetKey(handle, Config.bindAim) == GLFW.GLFW_PRESS;
            if (down && !aimBindWasPressed) Config.aimEnabled = !Config.aimEnabled;
            aimBindWasPressed = down;
        }
    }
}