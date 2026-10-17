package com.fox.bogempty;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import com.fox.bogempty.config.Config;
import com.fox.bogempty.module.combat.TriggerBot;
import com.fox.bogempty.module.combat.AimAssist;

public class BogEmpty implements ClientModInitializer {
    public static TriggerBot triggerBot;
    public static AimAssist aimAssist;
    private static boolean triggerBindWasPressed = false;
    private static boolean aimBindWasPressed = false;

    @Override
    public void onInitializeClient() {
        Config.load();
        triggerBot = new TriggerBot();
        aimAssist = new AimAssist();

        // Тик (обновляется 20 раз в секунду)
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            handleBinds(mc);
            triggerBot.onTick(mc);
        });

        // Рендер фрейм (обновляется 60+ раз в секунду для плавного Аима)
        WorldRenderEvents.START.register(ctx -> aimAssist.onFrame(MinecraftClient.getInstance()));
        
        System.out.println("[BogEmpty] Ядро загружено. Аим и Триггер на месте!");
    }

    private void handleBinds(MinecraftClient mc) {
        long handle = mc.getWindow().getHandle();
        
        if (mc.currentScreen != null) return;

        // Триггер
        if (Config.bindTrigger != -1) {
            boolean down = GLFW.glfwGetKey(handle, Config.bindTrigger) == GLFW.GLFW_PRESS;
            if (down && !triggerBindWasPressed) {
                Config.triggerEnabled = !Config.triggerEnabled;
            }
            triggerBindWasPressed = down;
        }

        // Аим
        if (Config.bindAim != -1) {
            boolean down = GLFW.glfwGetKey(handle, Config.bindAim) == GLFW.GLFW_PRESS;
            if (down && !aimBindWasPressed) {
                Config.aimEnabled = !Config.aimEnabled;
            }
            aimBindWasPressed = down;
        }
    }
}