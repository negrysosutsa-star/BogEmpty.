package com.fox.bogempty;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import com.fox.bogempty.config.Config;
import com.fox.bogempty.module.combat.TriggerBot;

public class BogEmpty implements ClientModInitializer {
    public static TriggerBot triggerBot;
    private static boolean bindWasPressed = false;

    @Override
    public void onInitializeClient() {
        Config.load();
        triggerBot = new TriggerBot();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            // Обработка бинда на Триггер (если задан)
            if (Config.bindTrigger != -1 && mc.currentScreen == null) {
                boolean down = GLFW.glfwGetKey(mc.getWindow().getHandle(), Config.bindTrigger) == GLFW.GLFW_PRESS;
                if (down && !bindWasPressed) {
                    Config.triggerEnabled = !Config.triggerEnabled;
                    System.out.println("[BogEmpty] Trigger: " + Config.triggerEnabled);
                }
                bindWasPressed = down;
            }

            // Запуск логики каждый тик
            triggerBot.onTick(mc);
        });
        
        System.out.println("[BogEmpty] Ядро загружено. Триггер готов!");
    }
}