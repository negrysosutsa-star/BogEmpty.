package com.fox.bogempty;

import com.fox.bogempty.config.Config;
import com.fox.bogempty.gui.ClickGUI;
import com.fox.bogempty.module.Module;
import com.fox.bogempty.module.combat.AimAssist;
import com.fox.bogempty.module.combat.TriggerBot;
import com.fox.bogempty.module.render.ESPModule;
import com.fox.bogempty.module.render.Fullbright;
import com.fox.bogempty.module.render.NoHurtCam;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
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
        Config.load();
        
        // Регистрация модулей
        modules.add(new TriggerBot());
        modules.add(new AimAssist());
        modules.add(new ESPModule());
        modules.add(new Fullbright());
        modules.add(new NoHurtCam());

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            long handle = mc.getWindow().getHandle();
            boolean down = GLFW.glfwGetKey(handle, bindGui) == GLFW.GLFW_PRESS;
            
            // Открытие/закрытие меню по правому шифту (344)
            if (down && !guiKeyWasDown) {
                if (mc.currentScreen == null) {
                    mc.setScreen(new ClickGUI());
                } else if (mc.currentScreen instanceof ClickGUI) {
                    mc.setScreen(null);
                }
            }
            guiKeyWasDown = down;

            // Тикаем все включенные модули
            for (Module m : modules) {
                if (m.isEnabled()) {
                    m.onTick();
                }
            }
        });

        // Рендер ESP
        WorldRenderEvents.LAST.register(ctx -> {
            for (Module m : modules) {
                if (m instanceof ESPModule esp && esp.isEnabled()) {
                    esp.onRender(ctx.matrixStack(), ctx.camera(), ctx.tickCounter().getTickDelta(true));
                }
            }
        });
    }
}