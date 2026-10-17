package com.fox.bogempty;

import com.fox.bogempty.gui.ClickGUI;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.module.combat.TriggerBot;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class BogEmpty implements ClientModInitializer {
    public static final List<Module> modules = new ArrayList<>();
    private static boolean guiKeyWasDown = false;
    public static int bindGui = 344;

    @Override
    public void onInitializeClient() {
        // Регистрация модулей
        modules.add(new TriggerBot());

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            mc.options.hudHidden = mc.currentScreen instanceof ClickGUI;
            
            long handle = mc.getWindow().getHandle();
            boolean down = GLFW.glfwGetKey(handle, bindGui) == GLFW.GLFW_PRESS;
            
            if (down && !guiKeyWasDown) {
                if (mc.currentScreen == null) mc.setScreen(new ClickGUI());
                else if (mc.currentScreen instanceof ClickGUI) mc.setScreen(null);
            }
            guiKeyWasDown = down;

            // Тикаем все включенные модули
            for (Module m : modules) {
                if (m.isEnabled()) {
                    m.onTick();
                }
            }
        });

        HudRenderCallback.EVENT.register((ctx, tickDelta) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc == null || mc.currentScreen instanceof ClickGUI) return;
            ctx.fill(5, 5, 105, 20, 0xFF161616);
            ctx.drawText(mc.textRenderer, "BogEmpty", 10, 9, 0xFF00E5FF, true);
        });
    }
}